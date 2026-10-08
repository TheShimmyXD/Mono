package com.jacck.mono.game

import com.jacck.mono.engine.Event
import com.jacck.mono.engine.JailCause
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.Jail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Recorridos de la ficha (FC.1, D-59) sobre el Clásico de 40 casillas. */
class WalkTest {

    private val config = Preset.CLASSIC.load()
    private val jail = config.squares.indexOfFirst { it is Jail }

    @Test
    fun `D-59 avanza casilla por casilla y da la vuelta por la salida`() {
        val w = motions(listOf(Event.Moved(0, 36, 3)), config, listOf(36, 0))
        assertEquals(listOf(Walk(0, listOf(36, 37, 38, 39, 0, 1, 2, 3))), w)
        assertEquals(HOP_MS, (w[0] as Walk).hopMs)
    }

    @Test
    fun `D-59 la carta de retroceder camina hacia atras`() {
        val back = config.cards.indexOfFirst { (it.effect as? CardEffect.MoveBy)?.let { m -> m.steps < 0 } == true }
        val w = motions(listOf(Event.Moved(1, 0, 7), Event.CardDrawn(1, back), Event.Moved(1, 7, 4)), config, listOf(0, 0))
        assertEquals(listOf(Walk(1, (0..7).toList()), Walk(1, listOf(7, 6, 5, 4))), w)
    }

    @Test
    fun `D-59 a la Carcel va de un salto despues de caminar`() {
        val w = motions(listOf(Event.Moved(0, 25, 30), Event.SentToJail(0, JailCause.SQUARE)), config, listOf(25, 0))
        assertEquals(listOf(Walk(0, (25..30).toList()), Walk(0, listOf(30, jail), jump = true)), w)
        // Por tres dobles no camina: salta desde donde está.
        assertEquals(listOf(Walk(0, listOf(12, jail), jump = true)), motions(listOf(Event.SentToJail(0, JailCause.DOUBLES)), config, listOf(12, 0)))
    }

    @Test
    fun `D-59 un recorrido largo no pasa de WALK_MAX_MS`() {
        val w = motions(listOf(Event.Moved(0, 5, 3)), config, listOf(5, 0)).single() as Walk
        assertEquals(38, w.path.size - 1)
        assertEquals(WALK_MAX_MS / 38, w.hopMs) // 71 ms por salto
    }

    @Test
    fun `D-65 la casilla comprada vuela al comprador despues de que la ficha llega`() {
        val w = motions(listOf(Event.Moved(1, 0, 3), Event.Bought(1, 3, 60)), config, listOf(0, 0))
        assertEquals(listOf(Walk(1, (0..3).toList()), Flight(1, 3)), w)
    }

    @Test
    fun `D-65 la subasta ganada vuela a quien gano, y una subasta sin vender no vuela`() {
        assertEquals(listOf(Flight(0, 39)), motions(listOf(Event.AuctionWon(0, 39, 210)), config, listOf(1, 39)))
        assertEquals(emptyList<Motion>(), motions(listOf(Event.AuctionUnsold(39)), config, listOf(1, 39)))
    }
}
