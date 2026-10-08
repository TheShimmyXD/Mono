package com.jacck.mono.game

import com.jacck.mono.engine.Event
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.Jail

/** Milisegundos por casilla del saltito (FC.1, maqueta A, D-59). */
const val HOP_MS = 180

/** El salto derecho a la Cárcel, que cruza el tablero (D-59). */
const val JUMP_MS = 500

/** Un recorrido no dura más que esto: uno largo (una carta que da la vuelta) va más rápido por casilla (D-59). */
const val WALK_MAX_MS = 2700

/** La casilla comprada: [TINT_MS] tomando el color del comprador y [FLY_MS] encogiéndose hasta su ícono (FD.1, D-65). */
const val TINT_MS = 450
const val FLY_MS = 750

/** Lo que la pantalla anima, de a uno y en orden, antes de abrir diálogos o dejar jugar a la máquina. */
sealed interface Motion {
    val player: Int
}

/**
 * Lo que la ficha de [player] recorre en el tablero (FC.1, D-59): [path] son las casillas, de la de
 * partida a la de llegada, y entre dos seguidas da un saltito. A la Cárcel va de un salto ([jump]):
 * `path` es solo de dónde sale y la Cárcel.
 */
data class Walk(override val player: Int, val path: List<Int>, val jump: Boolean = false) : Motion {
    /** Milisegundos de cada saltito: [HOP_MS], o menos si así el recorrido pasaría de [WALK_MAX_MS]; el salto, [JUMP_MS]. */
    val hopMs: Int get() = if (jump) JUMP_MS else minOf(HOP_MS, WALK_MAX_MS / maxOf(1, path.size - 1))
}

/** La casilla [square] pasa a [player] (compra o subasta ganada) y vuela hasta su ícono (FD.1, D-65). */
data class Flight(override val player: Int, val square: Int) : Motion

/**
 * Lo que dejan los eventos de una jugada, en orden: un `Moved` avanza casilla por casilla (o retrocede,
 * si lo mandó una carta de retroceder, D-14), un `SentToJail` salta derecho a la Cárcel desde donde
 * estaba y un `Bought` o un `AuctionWon` hace volar la casilla al ícono de quien se la queda.
 * [positions] son las casillas de cada jugador antes de la jugada.
 */
fun motions(events: List<Event>, config: GameConfig, positions: List<Int>): List<Motion> {
    val n = config.squares.size
    val jail = config.squares.indexOfFirst { it is Jail }
    val at = positions.toMutableList()
    var back = false
    val out = mutableListOf<Motion>()
    for (e in events) when (e) {
        is Event.CardDrawn -> back = (config.cards.getOrNull(e.card)?.effect as? CardEffect.MoveBy)?.let { it.steps < 0 } == true
        is Event.Moved -> {
            val steps = if (back) Math.floorMod(e.from - e.to, n) else Math.floorMod(e.to - e.from, n)
            val dir = if (back) -1 else 1
            if (steps > 0) out += Walk(e.player, (0..steps).map { Math.floorMod(e.from + dir * it, n) })
            at[e.player] = e.to
            back = false
        }
        is Event.SentToJail -> {
            if (at[e.player] != jail) out += Walk(e.player, listOf(at[e.player], jail), jump = true)
            at[e.player] = jail
        }
        is Event.Bought -> out += Flight(e.player, e.square)
        is Event.AuctionWon -> out += Flight(e.player, e.square)
        else -> Unit
    }
    return out
}
