package com.jacck.mono.engine

import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.NearestKind
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Utility

/** Baraja `items` con el generador de la partida (Fisher-Yates): misma semilla, mismo orden (D-03). */
fun GameRandom.shuffled(items: List<Int>): Pair<List<Int>, GameRandom> {
    val list = items.toMutableList()
    var random = this
    for (i in list.lastIndex downTo 1) {
        val (j, next) = random.nextInt(i + 1)
        random = next
        list[i] = list[j].also { list[j] = list[i] }
    }
    return list to random
}

/** Los mazos de `config.cards`, cada uno barajado al empezar la partida (R-18, D-14). */
internal fun shuffleDecks(config: GameConfig, random: GameRandom): Pair<Map<Deck, List<Int>>, GameRandom> {
    var next = random
    val decks = Deck.entries.associateWith { deck ->
        val (order, after) = next.shuffled(config.cards.indices.filter { config.cards[it].deck == deck })
        next = after
        order
    }
    return decks.filterValues { it.isNotEmpty() } to next
}

/**
 * Roba la carta de encima de `deck`, la devuelve debajo (o la guarda si es «Salir libre de la
 * Cárcel») y cumple lo que dice (R-18). Con el mazo vacío no pasa nada (D-14).
 */
internal fun drawCard(
    config: GameConfig,
    state: GameState,
    player: Int,
    deck: Deck,
    dice: Dice,
    events: MutableList<Event>,
): GameState {
    val order = state.decks[deck].orEmpty()
    val card = order.firstOrNull() ?: return state
    events += Event.CardDrawn(player, card)
    val effect = config.cards[card].effect
    val drawn = if (effect == CardEffect.GetOutOfJail) {
        val players = state.players.toMutableList()
        players[player] = players[player].copy(jailCards = players[player].jailCards + card)
        state.copy(players = players, decks = state.decks + (deck to order.drop(1)))
    } else {
        state.copy(decks = state.decks + (deck to order.drop(1) + card))
    }
    return cardEffect(config, drawn, player, effect, dice, events)
}

/** Devuelve la carta `card` debajo de su mazo (R-18). */
internal fun GameState.putUnder(config: GameConfig, card: Int): GameState {
    val deck = config.cards[card].deck
    return copy(decks = decks + (deck to decks[deck].orEmpty() + card))
}

/** Cumple el efecto de una carta (D-14); si mueve la ficha, cumple también lo de la casilla. */
private fun cardEffect(
    config: GameConfig,
    state: GameState,
    player: Int,
    effect: CardEffect,
    dice: Dice,
    events: MutableList<Event>,
): GameState {
    val size = config.squares.size
    val position = state.players[player].position
    val others = state.players.indices.filter { it != player && !state.players[it].bankrupt }
    return when (effect) {
        is CardEffect.Collect -> transfer(state, null, player, effect.amount, events)
        is CardEffect.Pay -> transfer(state, player, null, effect.amount, events)
        is CardEffect.MoveTo -> {
            // Siempre hacia adelante: si ya está en ella, da la vuelta entera.
            val steps = (effect.square - position - 1 + size) % size + 1
            land(config, Engine.advance(config, state, player, steps, events), player, dice, events)
        }
        is CardEffect.MoveBy -> {
            val moved = if (effect.steps >= 0) {
                Engine.advance(config, state, player, effect.steps, events)
            } else {
                moveBack(state, player, -effect.steps, size, events)
            }
            land(config, moved, player, dice, events)
        }
        CardEffect.GoToJail -> sendToJail(config, state, player, JailCause.CARD, events)
        CardEffect.GetOutOfJail -> state
        is CardEffect.Repairs -> {
            val mine = state.holdings.values.filter { it.owner == player }
            val amount = effect.perHouse * mine.sumOf { it.houses } + effect.perHotel * mine.count { it.hotel }
            transfer(state, player, null, amount, events)
        }
        is CardEffect.CollectFromEach -> others.fold(state) { s, o -> transfer(s, o, player, effect.amount, events) }
        is CardEffect.PayEach -> others.fold(state) { s, o -> transfer(s, player, o, effect.amount, events) }
        is CardEffect.MoveToNearest -> moveToNearest(config, state, player, effect, dice, events)
    }
}

/**
 * Avanza al ferrocarril o servicio más cercano (D-14). De otro: paga el alquiler × `rentFactor`,
 * o tira los dados y paga el total × `diceMultiplier`; del Banco o suyo: lo normal (R-11).
 */
private fun moveToNearest(
    config: GameConfig,
    state: GameState,
    player: Int,
    effect: CardEffect.MoveToNearest,
    dice: Dice,
    events: MutableList<Event>,
): GameState {
    val size = config.squares.size
    val position = state.players[player].position
    val steps = (1..size).firstOrNull {
        val square = config.squares[(position + it) % size]
        if (effect.kind == NearestKind.STATION) square is Station else square is Utility
    } ?: return state
    val moved = Engine.advance(config, state, player, steps, events)
    val target = moved.players[player].position
    val holding = moved.holdings[target]
    if (holding == null || holding.owner == player) return land(config, moved, player, dice, events)
    var after = moved
    val multiplier = effect.diceMultiplier
    val amount = when {
        holding.mortgaged && !config.rules.rentWhileMortgaged -> 0
        effect.kind == NearestKind.UTILITY && multiplier != null -> {
            val (thrown, next) = moved.random.rollDice()
            after = moved.copy(random = next)
            events += Event.DiceRolled(player, thrown)
            thrown.total * multiplier
        }
        else -> rentDue(config, moved, target, dice) * effect.rentFactor
    }
    if (amount == 0) return after
    events += Event.RentPaid(player, holding.owner, target, amount)
    return after.addMoney(player, -amount).addMoney(holding.owner, amount)
}

/** Retrocede `steps` casillas, sin cobrar el sueldo aunque pase por la salida (D-14). */
private fun moveBack(state: GameState, player: Int, steps: Int, size: Int, events: MutableList<Event>): GameState {
    val from = state.players[player].position
    val to = Math.floorMod(from - steps, size)
    events += Event.Moved(player, from, to)
    val players = state.players.toMutableList()
    players[player] = players[player].copy(position = to)
    return state.copy(players = players)
}

/** Pago por una carta: `null` es el Banco. El saldo puede quedar negativo hasta la quiebra (F2.6). */
private fun transfer(state: GameState, from: Int?, to: Int?, amount: Int, events: MutableList<Event>): GameState {
    if (amount == 0) return state
    events += Event.CardPayment(from, to, amount)
    val paid = if (from == null) state else state.addMoney(from, -amount)
    return if (to == null) paid else paid.addMoney(to, amount)
}
