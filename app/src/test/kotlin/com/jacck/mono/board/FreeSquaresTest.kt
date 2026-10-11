package com.jacck.mono.board

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FreeSquaresTest {

    private val config = Preset.CLASSIC.load()
    private val start = Engine.newGame(config, listOf("Ana", "Beto"), seed = 1).state

    @Test
    fun `al empezar estan libres todas las comprables y ninguna otra`() {
        val ownable = config.squares.indices.filter { config.squares[it] is OwnableSquare }.toSet()
        assertEquals(28, ownable.size)
        assertEquals(ownable, freeSquares(config, start))
    }

    @Test
    fun `con duenno, con casas o hipotecada deja de estar libre`() {
        val s = start.copy(holdings = mapOf(1 to Holding(0), 3 to Holding(0, houses = 2), 5 to Holding(1, mortgaged = true), 12 to Holding(1)))
        val free = freeSquares(config, s)
        assertEquals(24, free.size)
        assertTrue(free.none { it in setOf(1, 3, 5, 12) })
    }
}
