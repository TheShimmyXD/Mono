package com.jacck.mono.board

import kotlin.math.abs
import kotlin.math.ln

/** Lugar de una casilla en la rejilla del tablero: (0, 0) es la esquina de arriba a la izquierda. */
data class GridCell(val col: Int, val row: Int)

/**
 * Anillo rectangular de [cols] × [rows] (D-20): la salida abajo a la derecha y se avanza hacia la
 * izquierda, como en el tablero de cartón. Tiene 2·(cols + rows) − 4 casillas; el centro queda libre.
 */
data class RingGrid(val cols: Int, val rows: Int) {

    init {
        require(cols >= 2 && rows >= 2) { "un anillo necesita al menos 2 × 2" }
    }

    val size: Int get() = 2 * (cols + rows) - 4

    /** Casilla [i] del anillo: abajo de derecha a izquierda, sube por la izquierda, arriba hacia la derecha y baja. */
    fun cellOf(i: Int): GridCell {
        require(i in 0 until size) { "casilla $i fuera del anillo de $size" }
        return when {
            i < cols -> GridCell(cols - 1 - i, rows - 1)
            i < cols + rows - 1 -> GridCell(0, rows - 1 - (i - cols + 1))
            i < 2 * cols + rows - 2 -> GridCell(i - (cols + rows - 2), 0)
            else -> GridCell(cols - 1, i - (2 * cols + rows - 3))
        }
    }

    companion object {
        /** Columnas mínimas: en el centro (cols − 2 casillas de ancho) debe caber el panel del turno. */
        const val MIN_COLS = 5

        /** Filas mínimas: con menos, el centro no tiene alto. */
        const val MIN_ROWS = 3

        /**
         * El anillo de [n] casillas cuyas casillas son las más cuadradas en un espacio de [width] × [height]
         * (en cualquier unidad, la misma para los dos). D-09 pide [n] múltiplo de 4, pero el editor pasa
         * por tamaños impares al quitar o añadir (F4.3, D-41): entonces el anillo es de n + 1 y la última
         * celda, la de antes de la salida, queda vacía.
         */
        fun fit(n: Int, width: Float, height: Float): RingGrid {
            require(n >= 2 * (MIN_COLS + MIN_ROWS) - 4) { "no hay anillo de $n casillas" }
            require(width > 0f && height > 0f) { "espacio vacío" }
            val half = (n + n % 2 + 4) / 2 // cols + rows
            return (MIN_COLS..half - MIN_ROWS)
                .map { RingGrid(it, half - it) }
                .minBy { abs(ln((width / it.cols) / (height / it.rows))) }
        }
    }
}
