package com.jacck.mono.engine

/**
 * Azar inmutable con semilla (D-03): cada tirada devuelve el número y el generador
 * siguiente, que se guarda en el estado de la partida. Así una partida guardada o enviada
 * por Bluetooth se repite igual.
 *
 * Es SplitMix64, el mismo algoritmo de `java.util.SplittableRandom`, pero con el estado
 * en un `Long` visible para poder serializarlo.
 */
data class GameRandom(val state: Long) {

    /** Un `Long` uniforme y el generador para la siguiente tirada. */
    fun nextLong(): Pair<Long, GameRandom> {
        val next = state + GOLDEN_GAMMA
        var z = next
        z = (z xor (z ushr 30)) * MIX_1
        z = (z xor (z ushr 27)) * MIX_2
        return (z xor (z ushr 31)) to GameRandom(next)
    }

    /** Un entero en `0 until bound` y el generador para la siguiente tirada. */
    fun nextInt(bound: Int): Pair<Int, GameRandom> {
        require(bound > 0) { "bound debe ser positivo: $bound" }
        val (raw, next) = nextLong()
        // Sesgo del módulo < bound / 2^63: despreciable para dados y barajas.
        return ((raw ushr 1) % bound).toInt() to next
    }

    private companion object {
        const val GOLDEN_GAMMA = -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
        const val MIX_1 = -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
        const val MIX_2 = -0x6b2fb644ecceee15L // 0x94D049BB133111EB
    }
}
