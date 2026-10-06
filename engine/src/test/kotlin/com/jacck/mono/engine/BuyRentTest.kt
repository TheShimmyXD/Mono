package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Comprar y pagar alquiler (F2.3) en `propertyBoard`, con dados fijados. */
class BuyRentTest {

    private val classic = propertyBoard()
    private val anyDice = Dice(1, 2)

    private fun money(state: GameState) = state.players.map { it.money }

    @Test
    fun `R-11 al caer en una del Banco se ofrece y al comprarla paga el precio`() {
        val landed = Engine.roll(classic, twoPlayers(classic), Dice(1, 2)).state
        assertEquals(TurnPhase.Buy(3), landed.phase)
        val bought = Engine.apply(classic, landed, Action.Buy)
        assertEquals(Holding(owner = 0), bought.state.holdings[3])
        assertEquals(listOf(1440, 1500), money(bought.state))
        assertEquals(TurnPhase.EndOfTurn, bought.state.phase)
        assertEquals(listOf<Event>(Event.Bought(0, 3, 60)), bought.events)
    }

    @Test
    fun `R-11 sin dinero suficiente no se compra`() {
        val state = twoPlayers(classic, position = 3, phase = TurnPhase.Buy(3))
        val poor = state.copy(players = state.players.toMutableList().also { it[0] = it[0].copy(money = 59) })
        assertThrows<IllegalActionException> { Engine.apply(classic, poor, Action.Buy) }
    }

    @Test
    fun `D-08 si no la compra sigue del Banco (subasta en F2-6)`() {
        val state = twoPlayers(classic, position = 3, phase = TurnPhase.Buy(3))
        val declined = Engine.apply(classic, state, Action.Decline).state
        assertNull(declined.holdings[3])
        assertEquals(TurnPhase.EndOfTurn, declined.phase)
    }

    @Test
    fun `R-09 tras comprar con dobles vuelve a tirar`() {
        val landed = Engine.roll(classic, twoPlayers(classic), Dice(3, 3)).state
        assertEquals(TurnPhase.Buy(6), landed.phase)
        assertEquals(TurnPhase.Roll, Engine.apply(classic, landed, Action.Buy).state.phase)
    }

    @Test
    fun `R-13 paga al dueño el alquiler impreso`() {
        val result = Engine.roll(classic, twoPlayers(classic, mapOf(3 to Holding(1))), Dice(1, 2))
        assertEquals(listOf(1496, 1504), money(result.state))
        assertTrue(Event.RentPaid(0, 1, 3, 4) in result.events)
        assertEquals(TurnPhase.EndOfTurn, result.state.phase)
    }

    @Test
    fun `R-13 en la propia no paga`() {
        val result = Engine.roll(classic, twoPlayers(classic, mapOf(3 to Holding(0))), Dice(1, 2)).state
        assertEquals(listOf(1500, 1500), money(result))
    }

    @Test
    fun `R-14 la hipotecada no cobra alquiler`() {
        val state = twoPlayers(classic, mapOf(3 to Holding(1, mortgaged = true)))
        assertEquals(listOf(1500, 1500), money(Engine.roll(classic, state, Dice(1, 2)).state))
    }

    @Test
    fun `R-49 en Tio Rico la hipotecada sigue cobrando`() {
        val config = propertyBoard(tioRicoRules())
        val state = twoPlayers(config, mapOf(3 to Holding(1, mortgaged = true)))
        assertEquals(listOf(26396, 26404), money(Engine.roll(config, state, Dice(1, 2)).state))
    }

    @Test
    fun `R-15 con casas cobra el de su numero y con hotel el ultimo`() {
        val state = twoPlayers(classic, mapOf(1 to Holding(1, houses = 2), 3 to Holding(1, hotel = true)))
        assertEquals(30, rentDue(classic, state, 1, anyDice))
        assertEquals(450, rentDue(classic, state, 3, anyDice))
    }

    @Test
    fun `R-16 grupo completo sin construir cobra el doble aunque otro este hipotecado`() {
        val full = twoPlayers(classic, mapOf(1 to Holding(1, mortgaged = true), 3 to Holding(1)))
        assertEquals(8, rentDue(classic, full, 3, anyDice))
        assertEquals(4, rentDue(classic, twoPlayers(classic, mapOf(3 to Holding(1))), 3, anyDice))
        val noDouble = propertyBoard(testRules().copy(groupDoubleRent = false))
        assertEquals(4, rentDue(noDouble, full, 3, anyDice))
    }

    @Test
    fun `R-48 castillo en todas las del grupo cobra el doble`() {
        val config = propertyBoard(tioRicoRules())
        val both = twoPlayers(config, mapOf(1 to Holding(1, hotel = true), 3 to Holding(1, hotel = true)))
        assertEquals(900, rentDue(config, both, 3, anyDice))
        val one = twoPlayers(config, mapOf(1 to Holding(1, houses = 3), 3 to Holding(1, hotel = true)))
        assertEquals(450, rentDue(config, one, 3, anyDice))
        assertEquals(450, rentDue(classic, both, 3, anyDice))
    }

    @Test
    fun `R-13 ferrocarril cobra segun cuantos tiene el dueño (D-10)`() {
        assertEquals(25, rentDue(classic, twoPlayers(classic, mapOf(5 to Holding(1))), 5, anyDice))
        val two = twoPlayers(classic, mapOf(5 to Holding(1), 15 to Holding(1)))
        assertEquals(50, rentDue(classic, two, 5, anyDice))
    }

    @Test
    fun `R-13 servicio cobra los dados por su multiplicador (D-10)`() {
        val one = Engine.roll(classic, twoPlayers(classic, mapOf(7 to Holding(1))), Dice(3, 4)).state
        assertEquals(listOf(1472, 1528), money(one))
        val two = twoPlayers(classic, mapOf(7 to Holding(1), 12 to Holding(1)))
        assertEquals(70, rentDue(classic, two, 7, Dice(3, 4)))
    }
}
