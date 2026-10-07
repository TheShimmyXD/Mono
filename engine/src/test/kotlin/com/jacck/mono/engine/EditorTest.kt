package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Property
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Editor de casillas (F4.1, D-39) en `propertyBoard`: lo editado es lo que cobra el motor. */
class EditorTest {

    private val board = propertyBoard()
    private val red2 = board.squares[3] as Property

    @Test
    fun `R-13 una casilla editada cobra su alquiler nuevo`() {
        val edited = board.withSquare(3, red2.copy(name = "Las Cruces", rents = listOf(50, 100, 200, 300, 400, 500)))
        val result = Engine.roll(edited, twoPlayers(edited, mapOf(3 to Holding(1))), Dice(1, 2))
        assertEquals(listOf(1450, 1550), result.state.players.map { it.money })
        assertTrue(Event.RentPaid(0, 1, 3, 50) in result.events)
    }

    @Test
    fun `R-11 una casilla editada se compra a su precio nuevo`() {
        val edited = board.withSquare(3, red2.copy(price = 90))
        val landed = Engine.roll(edited, twoPlayers(edited), Dice(1, 2)).state
        val bought = Engine.apply(edited, landed, Action.Buy)
        assertEquals(listOf(Event.Bought(0, 3, 90)), bought.events)
    }

    @Test
    fun `withSquare cambia solo esa casilla y guarda la clave del dibujo`() {
        val edited = board.withSquare(3, red2.copy(name = "Otra", art = "rojo_2"))
        assertEquals(board.squares.filterIndexed { i, _ -> i != 3 }, edited.squares.filterIndexed { i, _ -> i != 3 })
        assertEquals("rojo_2", edited.squares[3].art)
        assertEquals("Rojo 2", board.squares[3].name)
    }

    @Test
    fun `D-09 una edicion fuera de rango la marca el validador`() {
        val edited = board.withSquare(3, red2.copy(name = "", rents = listOf(4, 20)))
        val errors = validate(edited)
        assertTrue(ConfigError.NameLength(3) in errors)
        assertTrue(errors.any { it is ConfigError.RentCount && it.square == 3 })
    }

    @Test
    fun `withSquare fuera del tablero es un error`() {
        assertThrows<IllegalArgumentException> { board.withSquare(16, red2) }
    }
}
