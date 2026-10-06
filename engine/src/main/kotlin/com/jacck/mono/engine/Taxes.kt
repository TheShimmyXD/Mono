package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rounding
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase

/**
 * Cae en un impuesto: con `percent` > 0 elige cómo pagarlo (R-19); si no, paga al Banco
 * `fixed` + `perHotel` por cada hotel o castillo suyo (R-52..R-54).
 */
internal fun landOnTax(state: GameState, player: Int, square: Int, tax: Tax, events: MutableList<Event>): GameState {
    if (tax.percent > 0) return state.copy(phase = TurnPhase.TaxChoice(square))
    return chargeTax(state, player, square, fixedTax(state, player, tax), events)
}

/**
 * Paga el impuesto en que cayó (R-19): `percent` % de su patrimonio, redondeado según
 * `percentRounding` (D-08), o la cifra fija. Luego sigue el turno (dobles, R-09).
 */
internal fun payTax(config: GameConfig, state: GameState, percent: Boolean): Result {
    val phase = state.phase as? TurnPhase.TaxChoice ?: throw IllegalActionException("no hay impuesto: ${state.phase}")
    val player = state.current
    val tax = config.squares[phase.square] as Tax
    val amount = if (percent) {
        percentOf(netWorth(config, state, player), tax.percent, config.rules.percentRounding)
    } else {
        fixedTax(state, player, tax)
    }
    val events = mutableListOf<Event>()
    val paid = chargeTax(state, player, phase.square, amount, events)
    return Result(Engine.finishMove(config, paid, events), events)
}

/**
 * Patrimonio para R-19: efectivo + precio impreso de sus casillas (hipotecadas o no) + lo que
 * costaron sus edificios. El hotel cuenta lo que se pagó por él y, si devolvió sus casas al
 * Banco (`hotelReturnsHouses`), también esas `maxHouses` casas (D-13).
 */
fun netWorth(config: GameConfig, state: GameState, player: Int): Int {
    var total = state.players[player].money
    for ((square, holding) in state.holdings) {
        if (holding.owner != player) continue
        val sq = config.squares[square] as OwnableSquare
        total += sq.price
        if (sq is Property) total += buildingsCost(config, sq, holding)
    }
    return total
}

/** `percent` % de `amount`, a $1 según `rounding` (R-32, D-08); NEAREST sube la mitad. */
internal fun percentOf(amount: Int, percent: Int, rounding: Rounding): Int {
    val hundredths = amount.toLong() * percent
    val value = when (rounding) {
        Rounding.UP -> Math.floorDiv(hundredths + 99, 100L)
        Rounding.DOWN -> Math.floorDiv(hundredths, 100L)
        Rounding.NEAREST -> Math.floorDiv(hundredths + 50, 100L)
    }
    return value.toInt()
}

/** Cifra fija del impuesto: `fixed` + `perHotel` por hotel o castillo de `player` (R-52). */
private fun fixedTax(state: GameState, player: Int, tax: Tax): Int =
    tax.fixed + tax.perHotel * state.holdings.values.count { it.owner == player && it.hotel }

/** Paga al Banco (R-52: «sin decir» → al Banco). El saldo puede quedar negativo (F2.6). */
private fun chargeTax(state: GameState, player: Int, square: Int, amount: Int, events: MutableList<Event>): GameState {
    events += Event.TaxPaid(player, square, amount)
    return state.addMoney(player, -amount)
}
