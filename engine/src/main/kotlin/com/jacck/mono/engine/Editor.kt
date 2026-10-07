package com.jacck.mono.engine

import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.model.Square

/**
 * El editor de tableros (F4): cambios puros sobre una configuración, que devuelven otra. No
 * valida: quien edita llama después a `validate` y muestra sus errores (D-09, F2.7).
 */

/** La configuración con la casilla [index] cambiada por [square] (F4.1, D-39); las demás quedan igual. */
fun GameConfig.withSquare(index: Int, square: Square): GameConfig {
    require(index in squares.indices) { "casilla $index fuera del tablero de ${squares.size}" }
    return copy(squares = squares.toMutableList().also { it[index] = square })
}

/** La configuración con las reglas [rules] (F4.2, D-40); el tablero queda igual. */
fun GameConfig.withRules(rules: RuleOptions): GameConfig = copy(rules = rules)

/**
 * La configuración con [square] insertada en la posición [index] (F4.3, D-41): las de [index] en
 * adelante corren un lugar y las cartas «Avance hasta…» siguen a su casilla. La salida queda en la 0.
 */
fun GameConfig.withSquareAdded(index: Int, square: Square): GameConfig {
    require(index in 1..squares.size) { "no se inserta en $index de un tablero de ${squares.size}" }
    return copy(
        squares = squares.toMutableList().also { it.add(index, square) },
        cards = cards.map { c -> (c.effect as? CardEffect.MoveTo)?.takeIf { it.square >= index }?.let { c.copy(effect = CardEffect.MoveTo(it.square + 1)) } ?: c },
    )
}

/**
 * La configuración sin la casilla [index] (F4.3, D-41): las siguientes corren un lugar atrás, las
 * cartas que llevaban a ella salen del mazo (su texto la nombra) y un grupo que queda sin propiedades
 * se borra. La salida (0) no se quita.
 */
fun GameConfig.withSquareRemoved(index: Int): GameConfig {
    require(index in 1 until squares.size) { "no se quita la casilla $index de un tablero de ${squares.size}" }
    val left = squares.filterIndexed { i, _ -> i != index }
    val used = left.filterIsInstance<Property>().map { it.group }.toSet()
    return copy(
        squares = left,
        groups = groups.filter { it.id in used || it.id !in squares.filterIsInstance<Property>().map { p -> p.group } },
        cards = cards.mapNotNull { c ->
            val move = c.effect as? CardEffect.MoveTo ?: return@mapNotNull c
            when {
                move.square == index -> null
                move.square > index -> c.copy(effect = CardEffect.MoveTo(move.square - 1))
                else -> c
            }
        },
    )
}
