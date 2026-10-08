package com.jacck.mono.game

import com.jacck.mono.engine.Event
import com.jacck.mono.engine.JailCause
import com.jacck.mono.engine.JailExit
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

    @Test
    fun `D-66 el alquiler lleva los billetes de quien paga a quien cobra despues de que la ficha llega`() {
        val w = motions(listOf(Event.Moved(0, 0, 3), Event.RentPaid(0, 1, 3, 4)), config, listOf(0, 3))
        assertEquals(listOf(Walk(0, (0..3).toList()), Payment(listOf(Transfer(0, 1, 4)))), w)
        assertEquals(mapOf(0 to -4, 1 to 4), (w[1] as Payment).deltas)
    }

    @Test
    fun `D-66 una carta que cobra a cada jugador sale en un solo pago`() {
        val events = listOf(
            Event.CardDrawn(2, 0), Event.CardPayment(0, 2, 10), Event.CardPayment(1, 2, 10), Event.CardPayment(3, 2, 10),
        )
        val w = motions(events, config, listOf(0, 0, 0, 0)).single() as Payment
        assertEquals(listOf(Transfer(0, 2, 10), Transfer(1, 2, 10), Transfer(3, 2, 10)), w.transfers)
        assertEquals(mapOf(0 to -10, 2 to 30, 1 to -10, 3 to -10), w.deltas)
    }

    @Test
    fun `D-68 el sueldo sale del Banco despues de caminar, y el alquiler que sigue va aparte`() {
        val events = listOf(Event.Moved(0, 38, 3), Event.SalaryPaid(0, 200), Event.RentPaid(0, 1, 3, 4))
        val w = motions(events, config, listOf(38, 3))
        assertEquals(
            listOf(Walk(0, listOf(38, 39, 0, 1, 2, 3)), Payment(listOf(Transfer(BANK, 0, 200))), Payment(listOf(Transfer(0, 1, 4)))),
            w,
        )
        assertEquals(mapOf(0 to 200), (w[1] as Payment).deltas)
        assertEquals(0, w[1].player)
    }

    @Test
    fun `D-68 impuesto, cartas del y al Banco y multa de la Carcel van con el Banco`() {
        val events = listOf(
            Event.TaxPaid(0, 4, 200), Event.CardDrawn(1, 0), Event.CardPayment(null, 1, 50),
            Event.CardDrawn(1, 1), Event.CardPayment(1, null, 15), Event.LeftJail(2, JailExit.FINE, 50),
            Event.LeftJail(3, JailExit.DOUBLES),
        )
        val w = motions(events, config, listOf(0, 0, jail, jail))
        assertEquals(
            listOf(Transfer(0, BANK, 200), Transfer(BANK, 1, 50), Transfer(1, BANK, 15), Transfer(2, BANK, 50)),
            w.map { (it as Payment).transfers.single() },
        )
        assertEquals(listOf(mapOf(0 to -200), mapOf(1 to 50), mapOf(1 to -15), mapOf(2 to -50)), w.map { (it as Payment).deltas })
    }

    @Test
    fun `D-68 el dinero que se ve no tiene el sueldo hasta que llegan los billetes del Banco`() {
        val queue = listOf(Walk(0, listOf(38, 39, 0)), Payment(listOf(Transfer(BANK, 0, 200))))
        assertEquals(listOf(1500, 1500), shownMoney(listOf(1700, 1500), queue, 0.9f))
        assertEquals(listOf(1650, 1500), shownMoney(listOf(1700, 1500), queue.drop(1), 0.75f))
    }

    @Test
    fun `D-66 el dinero que se ve no tiene los pagos en cola y cuenta el que se anima`() {
        // El estado ya tiene los dos pagos: 0 pagó 4 a 1 y después 1 pagó 50 a 0.
        val money = listOf(1546, 1454)
        val queue = listOf(Walk(0, listOf(0, 1)), Payment(listOf(Transfer(0, 1, 4))), Walk(1, listOf(1, 2)), Payment(listOf(Transfer(1, 0, 50))))
        assertEquals(listOf(1500, 1500), shownMoney(money, queue, 0.7f))
        assertEquals(listOf(1499, 1501), shownMoney(money, queue.drop(1), 0.25f))
        assertEquals(listOf(1496, 1504), shownMoney(money, queue.drop(1), 1f))
        assertEquals(listOf(1521, 1479), shownMoney(money, queue.drop(3), 0.5f))
        assertEquals(money, shownMoney(money, emptyList(), 0f))
    }
}
