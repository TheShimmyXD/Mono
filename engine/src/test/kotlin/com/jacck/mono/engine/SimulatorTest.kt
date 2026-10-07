package com.jacck.mono.engine

import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * F2.9 (D-19): 1000 partidas al azar con los dos presets, 2-6 jugadores y N de 16 a 48. Cada
 * acción pasa por los invariantes de `simulate` (dinero, saldos, existencias de edificios).
 */
class SimulatorTest {

    @Test
    fun `F2-9 mil partidas al azar sin excepciones y con el dinero cuadrado`() {
        val presets = listOf(Preset.CLASSIC.load(), Preset.TIO_RICO.load())
        val sizes = (16..48 step 4).toList()
        val maxTurns = 1000
        val results = mutableListOf<SimResult>()
        val start = System.nanoTime()
        for (g in 0 until 1000) {
            val config = ringBoard(presets[g % 2], sizes[(g / 10) % sizes.size], Random(g))
            results += simulate(config, players = 2 + (g / 2) % 5, seed = g.toLong(), maxTurns = maxTurns)
        }
        val seconds = (System.nanoTime() - start) / 1e9
        assertEquals(1000, results.size)
        val over = results.count { it.over }
        println(
            "SIM 1000 partidas en %.1f s · terminadas %d · al tope de %d turnos %d · acciones %d · quiebras %d"
                .format(seconds, over, maxTurns, 1000 - over, results.sumOf { it.actions }, results.sumOf { it.bankruptcies }),
        )
        presets.forEachIndexed { i, preset ->
            val mine = results.filterIndexed { g, _ -> g % 2 == i }
            val done = mine.filter { it.over }
            println("SIM ${preset.name}: terminadas ${done.size}/${mine.size}, turnos de media ${done.sumOf { it.turns } / maxOf(1, done.size)}")
        }
    }
}
