package com.jacck.mono.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Una semilla por partida (FF.1, D-84). */
class SeedsTest {

    @Test
    fun `FF-1 con el extra, la primera partida usa esa semilla y las siguientes no`() {
        val seeds = Seeds(7)
        assertEquals(7L, seeds.next())
        val rest = List(100) { seeds.next() }
        assertEquals(0, rest.count { it == 7L })
    }

    @Test
    fun `FF-1 sin el extra, 1000 semillas seguidas son todas distintas`() {
        val seeds = Seeds(null)
        assertEquals(1000, List(1000) { seeds.next() }.toSet().size)
    }

}
