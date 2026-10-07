package com.jacck.mono.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Geometría del tablero (D-20) para todos los N de D-09 y varias pantallas en vertical (dp). */
class RingGridTest {

    private val sizes = (16..48 step 4).toList()

    /** Redmi Note 13 Pro (≈ 393 × 840 dp útiles), un teléfono pequeño y uno grande. */
    private val screens = listOf(393f to 840f, 360f to 690f, 411f to 891f)

    @Test
    fun `el Clasico de 40 en el Redmi da 7 x 15 como la maqueta B`() {
        assertEquals(RingGrid(7, 15), RingGrid.fit(40, 393f, 840f))
    }

    @Test
    fun `los extremos 16 y 48 en el Redmi`() {
        assertEquals(RingGrid(5, 5), RingGrid.fit(16, 393f, 840f))
        assertEquals(RingGrid(8, 18), RingGrid.fit(48, 393f, 840f))
    }

    @Test
    fun `el anillo tiene N casillas y el centro deja sitio al panel`() {
        for (n in sizes) for ((w, h) in screens) {
            val g = RingGrid.fit(n, w, h)
            assertEquals(n, g.size, "N = $n en $w × $h")
            assertTrue(g.cols >= RingGrid.MIN_COLS && g.rows >= RingGrid.MIN_ROWS, "N = $n: $g")
            assertTrue(g.rows >= g.cols, "en vertical el anillo es más alto que ancho: $g")
        }
    }

    @Test
    fun `cada casilla cae en el borde, una por lugar, y la siguiente es vecina`() {
        for (n in sizes) for ((w, h) in screens) {
            val g = RingGrid.fit(n, w, h)
            val cells = (0 until n).map(g::cellOf)
            assertEquals(n, cells.toSet().size, "lugares repetidos con N = $n")
            for (c in cells) {
                assertTrue(c.col in 0 until g.cols && c.row in 0 until g.rows, "$c fuera de $g")
                assertTrue(c.col == 0 || c.col == g.cols - 1 || c.row == 0 || c.row == g.rows - 1, "$c no está en el borde")
            }
            for (i in 0 until n) {
                val a = cells[i]
                val b = cells[(i + 1) % n]
                assertEquals(1, Math.abs(a.col - b.col) + Math.abs(a.row - b.row), "N = $n: $i → ${i + 1} no son vecinas")
            }
        }
    }

    @Test
    fun `la salida va abajo a la derecha y se avanza hacia la izquierda`() {
        val g = RingGrid(7, 15)
        assertEquals(GridCell(6, 14), g.cellOf(0))
        assertEquals(GridCell(5, 14), g.cellOf(1))
        assertEquals(GridCell(0, 14), g.cellOf(6))
        assertEquals(GridCell(0, 0), g.cellOf(20))
        assertEquals(GridCell(6, 0), g.cellOf(26))
        assertEquals(GridCell(6, 13), g.cellOf(39))
    }

    @Test
    fun `fuera del anillo o con N imposible falla`() {
        assertThrows(IllegalArgumentException::class.java) { RingGrid(7, 15).cellOf(40) }
        assertThrows(IllegalArgumentException::class.java) { RingGrid.fit(10, 393f, 840f) }
    }

    @Test
    fun `con N impar el anillo es de N + 1 y queda vacia la celda de antes de la salida`() {
        val g = RingGrid.fit(39, 393f, 840f)
        assertEquals(40, g.size)
        assertEquals(39, (0 until 39).map { g.cellOf(it) }.toSet().size)
        assertEquals(GridCell(g.cols - 1, g.rows - 2), g.cellOf(39))
    }
}
