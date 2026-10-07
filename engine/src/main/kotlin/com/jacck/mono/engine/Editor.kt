package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
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
