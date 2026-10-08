package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** En el Clásico, Ana tiene los marrones (1 y 3) y la Estación de la Sabana (5) hipotecada. */
class PropertyMovesTest {

    private val config = Preset.CLASSIC.load()
    private val start = Engine.newGame(config, listOf("Ana", "Beto"), seed = 1).state.copy(current = 0)
    private fun state(vararg holdings: Pair<Int, Holding>) = start.copy(holdings = holdings.toMap())

    @Test
    fun `R-25 R-31 con el grupo sin casas se construye o se hipoteca`() {
        val s = state(1 to Holding(0), 3 to Holding(0))
        assertEquals(listOf(Move(Action.Build(1), 50), Move(Action.Mortgage(1), 30)), propertyMoves(config, s, 1))
    }

    @Test
    fun `R-26 R-30 con una casa se vende, y se construye en la otra`() {
        val s = state(1 to Holding(0, houses = 1), 3 to Holding(0))
        assertEquals(listOf(Move(Action.SellBuilding(1), 25)), propertyMoves(config, s, 1))
        assertEquals(listOf(Move(Action.Build(3), 50)), propertyMoves(config, s, 3))
    }

    @Test
    fun `R-32 levantar cuesta el valor más el 10 por ciento`() {
        val s = state(5 to Holding(0, mortgaged = true))
        assertEquals(listOf(Move(Action.Unmortgage(5), 110)), propertyMoves(config, s, 5))
    }

    @Test
    fun `R-27 con 4 casas en el grupo, el hotel`() {
        val s = state(1 to Holding(0, houses = 4), 3 to Holding(0, houses = 4))
        assertEquals(Move(Action.Build(1), 50, hotel = true), propertyMoves(config, s, 1).first())
    }

    @Test
    fun `con lo ajeno no hay jugadas`() {
        assertEquals(emptyList<Move>(), propertyMoves(config, state(11 to Holding(1)), 11))
    }

    @Test
    fun `D-60 la hoja propia en tu turno lleva jugadas, la de otro, la del otro teléfono o en una compra, solo se mira`() {
        assertTrue(canManage(start, 0, localTurn = true))
        assertTrue(canManage(start.copy(phase = TurnPhase.EndOfTurn), 0, localTurn = true))
        assertFalse(canManage(start, 1, localTurn = true))
        assertFalse(canManage(start, 0, localTurn = false))
        assertFalse(canManage(start.copy(phase = TurnPhase.Buy(1)), 0, localTurn = true))
    }
}
