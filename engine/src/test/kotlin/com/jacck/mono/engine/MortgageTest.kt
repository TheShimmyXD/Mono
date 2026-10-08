package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Hipotecas y venta de edificios al Banco (F2.6) en `propertyBoard`: rojo 1, 3; azul 6, 8, 9. */
class MortgageTest {

    private val config = propertyBoard()
    private val blue = mapOf(6 to Holding(0), 8 to Holding(0), 9 to Holding(0))

    @Test
    fun `R-31 hipoteca al valor impreso y la hipotecada no cobra`() {
        val (after, events) = Engine.apply(config, twoPlayers(config, mapOf(5 to Holding(0))), Action.Mortgage(5))
        assertEquals(1600, after.players[0].money)
        assertTrue(after.holdings.getValue(5).mortgaged)
        assertEquals(listOf(Event.Mortgaged(0, 5, 100)), events)
        assertEquals(0, rentDue(config, after, 5, Dice(1, 2)))
    }

    @Test
    fun `R-31 con edificios en el grupo antes hay que venderlos`() {
        val built = blue + (9 to Holding(0, houses = 1))
        assertThrows<IllegalActionException> { Engine.apply(config, twoPlayers(config, built), Action.Mortgage(6)) }
    }

    @Test
    fun `R-32 levantar cuesta la hipoteca mas el 10 por ciento, redondeado hacia arriba`() {
        // Azul 3: 55 + 5,5 → 55 + 6 = 61.
        val state = twoPlayers(config, blue + (9 to Holding(0, mortgaged = true)))
        val (after, _) = Engine.apply(config, state, Action.Unmortgage(9))
        assertEquals(1500 - 61, after.players[0].money)
        assertFalse(after.holdings.getValue(9).mortgaged)
    }

    @Test
    fun `R-32 con una hipotecada en el grupo no se construye`() {
        val state = twoPlayers(config, blue + (9 to Holding(0, mortgaged = true)))
        assertThrows<IllegalActionException> { Engine.apply(config, state, Action.Build(6)) }
    }

    @Test
    fun `R-30 vende una casa al Banco a la mitad`() {
        val state = twoPlayers(config, mapOf(6 to Holding(0, houses = 1), 8 to Holding(0, houses = 1), 9 to Holding(0, houses = 1)))
        val (after, events) = Engine.apply(config, state, Action.SellBuilding(6))
        assertEquals(1525, after.players[0].money)
        assertEquals(0, after.holdings.getValue(6).houses)
        assertEquals(state.bankHouses + 1, after.bankHouses)
        assertEquals(listOf(Event.BuildingSold(0, 6, 25)), events)
    }

    @Test
    fun `R-30 el hotel vuelve a 4 casas y cobra la mitad del hotel`() {
        val hotels = mapOf(6 to Holding(0, hotel = true), 8 to Holding(0, hotel = true), 9 to Holding(0, hotel = true))
        val (after, _) = Engine.apply(config, twoPlayers(config, hotels), Action.SellBuilding(6))
        assertEquals(Holding(0, houses = 4), after.holdings.getValue(6))
        assertEquals(1525, after.players[0].money)
    }

    @Test
    fun `R-26 se vende parejo, al reves de como se construyo`() {
        val uneven = mapOf(6 to Holding(0, houses = 1), 8 to Holding(0, houses = 2), 9 to Holding(0, houses = 2))
        assertThrows<IllegalActionException> { Engine.apply(config, twoPlayers(config, uneven), Action.SellBuilding(6)) }
        val (after, _) = Engine.apply(config, twoPlayers(config, uneven), Action.SellBuilding(8))
        assertEquals(1, after.holdings.getValue(8).houses)
    }

    @Test
    fun `R-49 Tio Rico hipoteca con casas por la mitad del total y sigue cobrando`() {
        val tio = propertyBoard(tioRicoRules())
        // Rojo 1: (60 + 2 casas × 1000) / 2 = 1030.
        val state = twoPlayers(tio, mapOf(1 to Holding(0, houses = 2), 3 to Holding(0)))
        val (after, _) = Engine.apply(tio, state, Action.Mortgage(1))
        assertEquals(26400 + 1030, after.players[0].money)
        assertEquals(30, rentDue(tio, after, 1, Dice(1, 2)))
    }

    @Test
    fun `R-50 Tio Rico levanta pagando la hipoteca mas $200`() {
        val tio = propertyBoard(tioRicoRules())
        val state = twoPlayers(tio, mapOf(1 to Holding(0, houses = 2, mortgaged = true)), phase = TurnPhase.EndOfTurn)
        val (after, _) = Engine.apply(tio, state, Action.Unmortgage(1))
        assertEquals(26400 - 1030 - 200, after.players[0].money)
    }
}
