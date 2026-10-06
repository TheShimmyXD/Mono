package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.model.Utility

/**
 * Efecto de caer en una casilla (R-08). La que se compra: del Banco → se ofrece (R-11); de
 * otro → paga el alquiler (R-13). Impuesto → R-19, R-52..R-54; «Váyase a la Cárcel» → R-20;
 * la Cárcel de visita (R-21) y Parada Libre (R-24) no hacen nada. Las cartas llegan en F2.5.
 * Si no alcanza el dinero, el saldo queda negativo hasta la quiebra (F2.6).
 */
internal fun land(config: GameConfig, state: GameState, player: Int, dice: Dice, events: MutableList<Event>): GameState {
    val square = state.players[player].position
    when (val sq = config.squares[square]) {
        is Tax -> return landOnTax(state, player, square, sq, events)
        is GoToJail -> return sendToJail(config, state, player, JailCause.SQUARE, events)
        !is OwnableSquare -> return state
        else -> {}
    }
    val holding = state.holdings[square] ?: return state.copy(phase = TurnPhase.Buy(square))
    if (holding.owner == player) return state
    val amount = rentDue(config, state, square, dice)
    if (amount == 0) return state
    events += Event.RentPaid(player, holding.owner, square, amount)
    return state.addMoney(player, -amount).addMoney(holding.owner, amount)
}

/** Compra al precio impreso la casilla en que cayó (R-11). */
internal fun buy(config: GameConfig, state: GameState): Result {
    val phase = state.phase as? TurnPhase.Buy ?: throw IllegalActionException("no hay nada que comprar: ${state.phase}")
    val price = (config.squares[phase.square] as OwnableSquare).price
    val player = state.current
    if (state.players[player].money < price) throw IllegalActionException("no alcanza: $price")
    val events = mutableListOf<Event>(Event.Bought(player, phase.square, price))
    val bought = state.addMoney(player, -price).copy(holdings = state.holdings + (phase.square to Holding(player)))
    return Result(Engine.finishMove(config, bought, events), events)
}

/** No la compra: sigue del Banco (D-08); la subasta (R-12, R-44) llega en F2.6. */
internal fun decline(config: GameConfig, state: GameState): Result {
    val phase = state.phase as? TurnPhase.Buy ?: throw IllegalActionException("no hay nada que rechazar: ${state.phase}")
    val events = mutableListOf<Event>(Event.Declined(state.current, phase.square))
    return Result(Engine.finishMove(config, state, events), events)
}

/**
 * Alquiler que cobra el dueño de `square` a quien cae con `dice` (R-13). Hipotecada: nada
 * (R-14) salvo `rentWhileMortgaged` (R-49). Ferrocarril y servicio: lo impreso según cuántos
 * tiene el dueño (D-10).
 */
fun rentDue(config: GameConfig, state: GameState, square: Int, dice: Dice): Int {
    val holding = state.holdings[square] ?: return 0
    if (holding.mortgaged && !config.rules.rentWhileMortgaged) return 0
    val owned = state.holdings.filterValues { it.owner == holding.owner }.keys
    return when (val sq = config.squares[square]) {
        is Property -> propertyRent(config, state, sq, holding, owned)
        is Station -> sq.rents.atCount(owned.count { config.squares[it] is Station })
        is Utility -> dice.total * sq.diceMultipliers.atCount(owned.count { config.squares[it] is Utility })
        else -> 0
    }
}

/**
 * Solar: hotel → el último de `rents` (×2 con hotel en todo el grupo, R-48); con casas → el
 * de su número (R-15); sin construir → el base, ×2 con el grupo completo (R-16).
 */
private fun propertyRent(
    config: GameConfig,
    state: GameState,
    property: Property,
    holding: Holding,
    owned: Set<Int>,
): Int {
    val group = groupSquares(config, property.group)
    val rules = config.rules
    return when {
        holding.hotel -> {
            val allHotels = group.all { state.holdings[it]?.hotel == true }
            property.rents.last() * if (rules.hotelGroupDoubleRent && allHotels) 2 else 1
        }
        holding.houses > 0 -> property.rents[holding.houses]
        else -> property.rents[0] * if (rules.groupDoubleRent && owned.containsAll(group)) 2 else 1
    }
}

/** Índices de los solares de un grupo de color. */
internal fun groupSquares(config: GameConfig, group: String): List<Int> =
    config.squares.indices.filter { (config.squares[it] as? Property)?.group == group }

/** Alquiler con `count` del mismo dueño; si el tablero trae más que cifras, la última. */
private fun List<Int>.atCount(count: Int): Int = this[(count - 1).coerceIn(0, lastIndex)]
