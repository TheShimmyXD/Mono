package com.jacck.mono.game

import com.jacck.mono.engine.Dice
import com.jacck.mono.engine.Event
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

/** La bitácora de la ventana «Lo que pasa» (FD.5, D-71): una tarjeta por turno, las 2 últimas. */
class TurnLogTest {

    private val roll = Event.DiceRolled(0, Dice(6, 6))
    private val again = Event.RollAgain(0)
    private val roll2 = Event.DiceRolled(0, Dice(2, 1))
    private val end = Event.TurnPassed(0)

    @Test
    fun `FD-5 las jugadas del mismo turno se suman a su tarjeta, con los dobles`() {
        var log = logged(emptyList(), 0, listOf(roll, again))
        log = logged(log, 0, listOf(roll2))
        log = logged(log, 0, listOf(end))
        assertEquals(listOf(TurnLog(0, listOf(roll, again, roll2, end))), log)
    }

    @Test
    fun `FD-5 el turno de otro abre una tarjeta nueva`() {
        val log = logged(logged(emptyList(), 0, listOf(roll)), 1, listOf(Event.DiceRolled(1, Dice(4, 3))))
        assertEquals(listOf(0, 1), log.map { it.player })
    }

    @Test
    fun `FD-5 quedan las 2 ultimas tarjetas`() {
        var log = emptyList<TurnLog>()
        listOf(0, 1, 2, 0).forEach { p -> log = logged(log, p, listOf(Event.TurnPassed(p))) }
        assertEquals(listOf(2, 0), log.map { it.player })
    }

    @Test
    fun `FD-5 una jugada sin eventos no cambia la bitacora`() {
        val log = logged(emptyList(), 0, listOf(roll))
        assertSame(log, logged(log, 1, emptyList()))
    }
}
