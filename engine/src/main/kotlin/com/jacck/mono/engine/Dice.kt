package com.jacck.mono.engine

import kotlinx.serialization.Serializable

/** Una tirada de los dos dados (R-07). */
@Serializable
data class Dice(val first: Int, val second: Int) {

    init {
        require(first in 1..6 && second in 1..6) { "dado fuera de 1..6: $first, $second" }
    }

    val total: Int get() = first + second

    /** Dobles (R-09) o «par» de Tío Rico (R-43, D-08). */
    val isDouble: Boolean get() = first == second
}

/** Tira los dos dados con el generador de la partida y devuelve el generador siguiente. */
fun GameRandom.rollDice(): Pair<Dice, GameRandom> {
    val (first, afterFirst) = nextInt(6)
    val (second, afterSecond) = afterFirst.nextInt(6)
    return Dice(first + 1, second + 1) to afterSecond
}

/**
 * Dados que salen del generador, para lo que tira un número de veces que no se sabe antes
 * (la tirada inicial con empates). Al terminar, `random` es el generador que sigue.
 */
internal class SeededDice(var random: GameRandom) : Iterator<Dice> {

    override fun hasNext(): Boolean = true

    override fun next(): Dice {
        val (dice, next) = random.rollDice()
        random = next
        return dice
    }
}
