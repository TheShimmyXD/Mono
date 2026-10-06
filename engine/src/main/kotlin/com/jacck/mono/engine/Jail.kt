package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.TurnPhase

/**
 * Va directo a la Cárcel: sin avanzar ni cobrar el sueldo aunque pase la salida, y termina el
 * turno aunque haya sacado dobles (R-20). `jailTurns` = 0: aún no ha pasado ningún turno dentro.
 * Un tablero sin Cárcel no llega aquí: el validador (F2.7) lo rechaza.
 */
internal fun sendToJail(
    config: GameConfig,
    state: GameState,
    player: Int,
    cause: JailCause,
    events: MutableList<Event>,
): GameState {
    check(config.rules.jail) { "las reglas no tienen Cárcel" }
    val jail = config.squares.indexOfFirst { it is Jail }
    check(jail >= 0) { "el tablero no tiene Cárcel" }
    events += Event.SentToJail(player, cause)
    val players = state.players.toMutableList()
    players[player] = players[player].copy(position = jail, jailTurns = 0)
    return state.copy(players = players, doublesInRow = 0, phase = TurnPhase.EndOfTurn)
}

/**
 * Tirada desde la Cárcel (R-22): con dobles sale y avanza lo que marcan, sin volver a tirar.
 * Sin dobles se queda; en el turno número `jailMaxTurns` paga la multa a la fuerza, sale y
 * avanza lo que marcó (el saldo puede quedar negativo hasta la quiebra, F2.6).
 */
internal fun rollInJail(config: GameConfig, state: GameState, dice: Dice): Result {
    val rules = config.rules
    val player = state.current
    val events = mutableListOf<Event>(Event.DiceRolled(player, dice))
    val turns = state.players[player].jailTurns!! + 1
    val way = when {
        dice.isDouble -> JailExit.DOUBLES
        turns >= rules.jailMaxTurns -> JailExit.LAST_TURN
        else -> {
            events += Event.StayedInJail(player, turns)
            val stayed = state.withJailTurns(player, turns).copy(phase = TurnPhase.EndOfTurn)
            return Result(stayed, events)
        }
    }
    val fine = if (way == JailExit.LAST_TURN) rules.jailFine else 0
    events += Event.LeftJail(player, way, fine)
    val free = state.withJailTurns(player, null).addMoney(player, -fine).copy(doublesInRow = 0)
    val moved = Engine.advance(config, free, player, dice.total, events)
    val landed = land(config, moved, player, dice, events)
    // Los dobles que lo sacaron no dan otra tirada (R-22).
    return Result(if (landed.phase == TurnPhase.Roll) landed.copy(phase = TurnPhase.EndOfTurn) else landed, events)
}

/**
 * Paga la multa antes de tirar, en uno de los `jailMaxTurns` − 1 turnos siguientes a entrar
 * (R-22: «cualquiera de sus dos turnos siguientes»); luego tira normal.
 */
internal fun payJailFine(config: GameConfig, state: GameState): Result {
    val player = state.current
    val turns = jailedBeforeRoll(state)
    val fine = config.rules.jailFine
    if (turns >= config.rules.jailMaxTurns - 1) throw IllegalActionException("en el último turno se tira (R-22)")
    if (state.players[player].money < fine) throw IllegalActionException("no alcanza: $fine")
    val free = state.withJailTurns(player, null).addMoney(player, -fine)
    return Result(free, listOf(Event.LeftJail(player, JailExit.FINE, fine)))
}

/**
 * Usa una carta «Salir libre de la Cárcel» antes de tirar y luego tira normal (R-22); la
 * carta vuelve debajo de su mazo (R-18).
 */
internal fun useJailCard(config: GameConfig, state: GameState): Result {
    val player = state.current
    jailedBeforeRoll(state)
    val p = state.players[player]
    val card = p.jailCards.firstOrNull() ?: throw IllegalActionException("no tiene la carta")
    val players = state.players.toMutableList()
    players[player] = p.copy(jailTurns = null, jailCards = p.jailCards.drop(1))
    val freed = state.copy(players = players).putUnder(config, card)
    return Result(freed, listOf(Event.LeftJail(player, JailExit.CARD)))
}

/** Turnos que lleva dentro quien juega, si está en la Cárcel y aún no ha tirado. */
private fun jailedBeforeRoll(state: GameState): Int {
    if (state.phase != TurnPhase.Roll) throw IllegalActionException("solo antes de tirar: ${state.phase}")
    return state.players[state.current].jailTurns ?: throw IllegalActionException("no está en la Cárcel")
}

private fun GameState.withJailTurns(player: Int, turns: Int?): GameState {
    val list = players.toMutableList()
    list[player] = list[player].copy(jailTurns = turns)
    return copy(players = list)
}
