package com.jacck.mono.game

import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.validate
import com.jacck.mono.engine.withSquareAdded
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** La casilla nueva del editor (F4.3, D-41) en el Clásico. */
class NewSquareTest {

    private val clasico = Preset.CLASSIC.load()

    @Test
    fun `la propiedad nueva copia la de antes y se llama como se pide`() {
        val sq = newSquare(clasico, NewKind.PROPERTY, 4, "Casilla nueva") as Property
        val before = clasico.squares[3] as Property
        assertEquals(before.copy(name = "Casilla nueva", art = null), sq)
    }

    @Test
    fun `carta, impuesto y tren copian el mas cercano hacia atras, dando la vuelta`() {
        assertEquals(clasico.squares[2], newSquare(clasico, NewKind.CARD, 4, "x"))
        assertEquals(clasico.squares[38], newSquare(clasico, NewKind.TAX, 1, "x"))
        assertEquals((clasico.squares[35] as Station).copy(name = "x"), newSquare(clasico, NewKind.STATION, 3, "x"))
        assertTrue(newSquare(clasico, NewKind.CARD, 4, "x") is CardSquare && newSquare(clasico, NewKind.TAX, 1, "x") is Tax)
    }

    @Test
    fun `sin casillas de ese tipo no hay casilla nueva`() {
        val sinImpuestos = clasico.copy(squares = clasico.squares.filter { it !is Tax })
        assertNull(newSquare(sinImpuestos, NewKind.TAX, 3, "x"))
    }

    @Test
    fun `cuatro propiedades nuevas en grupos distintos dejan el Clasico de 44 valido`() {
        val added = listOf(31, 21, 11, 1).fold(clasico) { c, i -> c.withSquareAdded(i + 1, newSquare(c, NewKind.PROPERTY, i, "Casilla nueva")!!) }
        assertEquals(44, added.squares.size)
        assertEquals(emptyList<Any>(), validate(added))
    }

    @Test
    fun `la salida y la carcel no se quitan`() {
        assertFalse(removable(clasico, 0))
        assertFalse(removable(clasico, 10))
        assertTrue(removable(clasico, 1))
    }
}
