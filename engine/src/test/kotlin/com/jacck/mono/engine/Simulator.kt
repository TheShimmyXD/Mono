package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.TurnPhase
import kotlin.random.Random

/**
 * Simulador de F2.9 (D-19): jugadores al azar contra el motor, comprobando invariantes en cada
 * acción. Vive en las pruebas: el azar de los jugadores (`kotlin.random`) no es el del motor (D-03).
 */

/** Resultado de una partida simulada. */
data class SimResult(val over: Boolean, val turns: Int, val actions: Int, val bankruptcies: Int)

/** Un invariante roto: partida, acción y qué pasó. */
class InvariantBroken(message: String) : AssertionError(message)

/**
 * `base` con N = `n` casillas: quita casillas al azar (nunca la salida ni la Cárcel) o añade
 * descansos. Las cartas que mandan a una casilla quitada se quitan; los grupos vacíos, también.
 */
fun ringBoard(base: GameConfig, n: Int, random: Random): GameConfig {
    if (n == base.squares.size) return base
    val kept = base.squares.indices.toMutableList()
    val removable = kept.filter { it != 0 && base.squares[it] !is Jail }.shuffled(random)
    kept.removeAll(removable.take(maxOf(0, base.squares.size - n)).toSet())
    val squares: MutableList<Square> = kept.map { base.squares[it] }.toMutableList()
    val newIndex = kept.withIndex().associate { (new, old) -> old to new }.toMutableMap()
    repeat(maxOf(0, n - base.squares.size)) { k ->
        val at = 1 + random.nextInt(squares.size)
        squares.add(at, Rest("Descanso $k"))
        newIndex.replaceAll { _, i -> if (i >= at) i + 1 else i }
    }
    val cards = base.cards.mapNotNull { card ->
        when (val e = card.effect) {
            is CardEffect.MoveTo -> newIndex[e.square]?.let { card.copy(effect = CardEffect.MoveTo(it)) }
            else -> card
        }
    }
    val groups = base.groups.filter { g -> squares.any { it is Property && it.group == g.id } }
    var config = base.copy(name = "${base.name} $n", groups = groups, squares = squares, cards = cards)
    // Cartas sin destino (p. ej. sin estaciones) fuera; mazo vacío → sus casillas, descansos.
    val badCards = validate(config).filterIsInstance<ConfigError.BadCard>().map { it.card }.toSet()
    config = config.copy(cards = config.cards.filterIndexed { i, _ -> i !in badCards })
    val emptyDecks = validate(config).filterIsInstance<ConfigError.EmptyDeck>().map { it.deck }.toSet()
    config = config.copy(squares = config.squares.map { if (it is CardSquare && it.deck in emptyDecks) Rest(it.name) else it })
    val errors = validate(config)
    check(errors.isEmpty()) { "tablero de $n casillas inválido: $errors" }
    return config
}

/** Juega una partida con `players` jugadores al azar hasta que termine o llegue a `maxTurns`. */
fun simulate(config: GameConfig, players: Int, seed: Long, maxTurns: Int): SimResult {
    val random = Random(seed)
    var state = Engine.newGame(config, List(players) { "J$it" }, seed).state
    var actions = 0
    var extras = 0
    var lastTurn = state.turn
    while (state.phase !is TurnPhase.Over && state.turn < maxTurns) {
        if (state.turn != lastTurn) { extras = 0; lastTurn = state.turn }
        val options = candidates(config, state, random, extras)
        var next: Result? = null
        var chosen: Action? = null
        for (action in options) {
            next = try { Engine.apply(config, state, action) } catch (_: IllegalActionException) { null }
            if (next != null) { chosen = action; break }
        }
        val where = "semilla $seed, acción $actions, ${state.phase}"
        if (next == null || chosen == null) throw InvariantBroken("$where: ninguna acción válida entre $options")
        if (state.phase == TurnPhase.EndOfTurn && chosen != Action.EndTurn) extras++
        checkMoney(state, next, where)
        checkStock(config, next.state, where)
        state = next.state
        actions++
        if (actions > maxTurns * 200) throw InvariantBroken("$where: más de ${maxTurns * 200} acciones")
    }
    return SimResult(state.phase is TurnPhase.Over, state.turn, actions, state.players.count { it.bankrupt })
}

/** Acciones a probar en orden; el motor dice cuál es válida (`IllegalActionException`). */
private fun candidates(config: GameConfig, state: GameState, random: Random, extras: Int): List<Action> {
    val me = state.current
    fun owned(player: Int) = state.holdings.filterValues { it.owner == player }.keys.toList()
    return when (val phase = state.phase) {
        TurnPhase.Roll -> if (state.players[me].jailTurns != null) {
            listOf(Action.Roll, Action.PayJailFine, Action.UseJailCard).shuffled(random) + Action.Roll
        } else listOf(Action.Roll)
        is TurnPhase.Buy -> if (random.nextInt(10) < 7) listOf(Action.Buy, Action.Decline) else listOf(Action.Decline)
        is TurnPhase.Auction -> phase.bidders.shuffled(random).flatMap { b ->
            val bid = Action.Bid(b, phase.highestBid + 1 + random.nextInt(100))
            if (random.nextInt(3) == 0) listOf(bid, Action.PassBid(b)) else listOf(Action.PassBid(b), bid)
        } + phase.bidders.map { Action.Bid(it, phase.highestBid + 1) }
        is TurnPhase.TaxChoice -> random.nextBoolean().let { listOf(Action.PayTax(it), Action.PayTax(!it)) }
        TurnPhase.EndOfTurn -> {
            val mine = owned(me)
            val extra = if (extras < 3 && random.nextInt(2) == 0) {
                (mine.map { Action.Build(it) } + mine.map { Action.Unmortgage(it) }).shuffled(random)
            } else emptyList()
            extra + Action.EndTurn
        }
        is TurnPhase.Debt -> {
            val mine = owned(phase.debts.first().debtor)
            (mine.map { Action.SellBuilding(it) } + mine.map { Action.Mortgage(it) }).shuffled(random) + Action.DeclareBankruptcy
        }
        is TurnPhase.Over -> emptyList()
    }
}

/**
 * Cada saldo cambia lo que dicen los eventos de la acción (el Banco paga y cobra sin límite, R-05).
 * En una quiebra el traspaso no lleva cifra: se auditan los demás y el quebrado queda en $0.
 * Fuera de una deuda, nadie en juego tiene saldo negativo.
 */
private fun checkMoney(before: GameState, result: Result, where: String) {
    val after = result.state
    val delta = IntArray(before.players.size)
    val skip = mutableSetOf<Int>()
    for (e in result.events) when (e) {
        is Event.SalaryPaid -> delta[e.player] += e.amount
        is Event.Bought -> delta[e.player] -= e.price
        is Event.AuctionWon -> delta[e.player] -= e.amount
        is Event.RentPaid -> { delta[e.payer] -= e.amount; delta[e.owner] += e.amount }
        is Event.HouseBuilt -> delta[e.player] -= e.price
        is Event.HotelBuilt -> delta[e.player] -= e.price
        is Event.TaxPaid -> delta[e.player] -= e.amount
        is Event.CardPayment -> { e.from?.let { delta[it] -= e.amount }; e.to?.let { delta[it] += e.amount } }
        is Event.LeftJail -> delta[e.player] -= e.fine
        is Event.Mortgaged -> delta[e.player] += e.amount
        is Event.Unmortgaged -> delta[e.player] -= e.amount
        is Event.BuildingSold -> delta[e.player] += e.amount
        is Event.DeedDealt -> delta[e.player] -= e.price
        is Event.Bankrupt -> {
            skip += e.player
            e.creditor?.let { skip += it }
            if (after.players[e.player].money != 0) throw InvariantBroken("$where: el quebrado quedó con ${after.players[e.player].money}")
        }
        else -> Unit
    }
    for (p in before.players.indices) {
        if (p in skip) continue
        val real = after.players[p].money - before.players[p].money
        if (real != delta[p]) throw InvariantBroken("$where: J$p cambió $real y los eventos dicen ${delta[p]}; ${result.events}")
    }
    if (after.phase !is TurnPhase.Debt) {
        after.players.forEachIndexed { p, pl ->
            if (!pl.bankrupt && pl.money < 0) throw InvariantBroken("$where: J$p con ${pl.money} fuera de una deuda")
        }
    }
}

/** Casas y hoteles: los del Banco más los del tablero = las existencias (R-28). */
private fun checkStock(config: GameConfig, state: GameState, where: String) {
    val rules = config.rules
    val houses = state.holdings.values.sumOf { it.houses }
    val hotels = state.holdings.values.count { it.hotel }
    if (rules.houseStock > 0 && state.bankHouses + houses != rules.houseStock) {
        throw InvariantBroken("$where: casas ${state.bankHouses} + $houses ≠ ${rules.houseStock}")
    }
    if (rules.hotelStock > 0 && state.bankHotels + hotels != rules.hotelStock) {
        throw InvariantBroken("$where: hoteles ${state.bankHotels} + $hotels ≠ ${rules.hotelStock}")
    }
}
