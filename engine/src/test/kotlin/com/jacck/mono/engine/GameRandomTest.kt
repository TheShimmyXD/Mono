package com.jacck.mono.engine

import java.util.SplittableRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Azar con semilla (D-03). La referencia es `SplittableRandom` del JDK, no el motor. */
class GameRandomTest {

    @Test
    fun `D-03 coincide con SplittableRandom del JDK`() {
        for (seed in listOf(0L, 42L, -1L, Long.MAX_VALUE)) {
            val reference = SplittableRandom(seed)
            var random = GameRandom(seed)
            repeat(1000) {
                val (value, next) = random.nextLong()
                assertEquals(reference.nextLong(), value, "semilla $seed, tirada $it")
                random = next
            }
        }
    }

    @Test
    fun `D-03 la misma semilla da la misma secuencia`() {
        fun sequence(seed: Long): List<Int> {
            var random = GameRandom(seed)
            return List(50) { random.nextInt(6).also { (_, next) -> random = next }.first }
        }
        assertEquals(sequence(7L), sequence(7L))
        assertTrue(sequence(7L) != sequence(8L))
    }

    @Test
    fun `D-03 tirar no cambia el generador`() {
        val random = GameRandom(123L)
        random.nextInt(6)
        assertEquals(GameRandom(123L), random)
    }

    @Test
    fun `D-03 nextInt queda en el rango y sale cada valor`() {
        var random = GameRandom(2026L)
        val counts = IntArray(6)
        repeat(6000) {
            val (value, next) = random.nextInt(6)
            counts[value]++
            random = next
        }
        // 6000 tiradas: cada cara ~1000; con 800-1200 la prueba no es frágil.
        assertTrue(counts.all { it in 800..1200 }, counts.joinToString())
    }
}
