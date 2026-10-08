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

/** Un pago entre jugadores: los billetes van de un ícono al otro y el dinero cuenta en este tiempo (FD.2, D-66). */
const val PAY_MS = 1500

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

/** [from] le paga [amount] a [to] (alquiler o carta, FD.2, D-66). */
data class Transfer(val from: Int, val to: Int, val amount: Int)

/**
 * Pagos entre jugadores que salen a la vez (FD.2, D-66): los de una misma jugada que van seguidos, como
 * una carta que cobra a cada jugador. [player] es quien paga el primero.
 */
data class Payment(val transfers: List<Transfer>) : Motion {
    override val player: Int get() = transfers.first().from

    /** Cuánto gana (+) o pierde (−) cada jugador con estos pagos; quien no paga ni cobra no sale. */
    val deltas: Map<Int, Int>
        get() = buildMap {
            transfers.forEach { (from, to, amount) ->
                put(from, (get(from) ?: 0) - amount)
                put(to, (get(to) ?: 0) + amount)
            }
        }
}

/**
 * El dinero que se ve mientras se anima la cola [queue] (FD.2, D-66): el de [money] (el del estado, que ya
 * tiene todos los pagos) sin los pagos que aún no se animan; el primero de la cola, si es un pago, va a [f]
 * (0 a 1) del camino, redondeado.
 */
fun shownMoney(money: List<Int>, queue: List<Motion>, f: Float): List<Int> {
    val out = money.toMutableList()
    queue.filterIsInstance<Payment>().forEach { p -> p.deltas.forEach { (k, d) -> out[k] -= d } }
    (queue.firstOrNull() as? Payment)?.deltas?.forEach { (k, d) -> out[k] += Math.round(d * f.coerceIn(0f, 1f)) }
    return out
}

/**
 * Lo que dejan los eventos de una jugada, en orden: un `Moved` avanza casilla por casilla (o retrocede,
 * si lo mandó una carta de retroceder, D-14), un `SentToJail` salta derecho a la Cárcel desde donde
 * estaba, un `Bought` o un `AuctionWon` hace volar la casilla al ícono de quien se la queda y un
 * `RentPaid` o un `CardPayment` entre dos jugadores lleva los billetes de uno al otro (los pagos con el
 * Banco, FD.3, todavía no).
 * [positions] son las casillas de cada jugador antes de la jugada.
 */
fun motions(events: List<Event>, config: GameConfig, positions: List<Int>): List<Motion> {
    val n = config.squares.size
    val jail = config.squares.indexOfFirst { it is Jail }
    val at = positions.toMutableList()
    var back = false
    val out = mutableListOf<Motion>()
    // Un pago se junta con el anterior si este fue el último movimiento: salen a la vez.
    fun pay(t: Transfer) {
        val last = out.lastOrNull()
        if (last is Payment) out[out.lastIndex] = Payment(last.transfers + t) else out += Payment(listOf(t))
    }
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
        is Event.RentPaid -> pay(Transfer(e.payer, e.owner, e.amount))
        is Event.CardPayment -> {
            val (from, to) = e.from to e.to
            if (from != null && to != null) pay(Transfer(from, to, e.amount))
        }
        else -> Unit
    }
    return out
}
