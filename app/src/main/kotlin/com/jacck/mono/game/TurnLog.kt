package com.jacck.mono.game

import com.jacck.mono.engine.Event

/** Un turno de la ventana «Lo que pasa» (FD.5, D-71): de quién es y lo que pasó en él, en orden. */
data class TurnLog(val player: Int, val events: List<Event>)

/** Tarjetas que guarda la ventana: el turno de ahora y el anterior (D-71). */
const val TURNS_SHOWN = 2

/**
 * La bitácora después de una jugada en el turno de [player] que dejó [events]: si la última tarjeta
 * es de [player], se le suman (sus dobles, su compra, la subasta de su casilla); si no, una tarjeta
 * nueva. Quedan las últimas [keep]. Sin eventos, la bitácora no cambia.
 */
fun logged(log: List<TurnLog>, player: Int, events: List<Event>, keep: Int = TURNS_SHOWN): List<TurnLog> {
    if (events.isEmpty()) return log
    val last = log.lastOrNull()
    val next = if (last?.player == player) log.dropLast(1) + last.copy(events = last.events + events) else log + TurnLog(player, events)
    return next.takeLast(keep)
}
