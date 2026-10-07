package com.jacck.mono.game

import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax

/** Tipos de casilla que se añaden desde la ficha (F4.3, D-41). */
enum class NewKind { PROPERTY, STATION, CARD, TAX }

/**
 * La casilla nueva de tipo [kind] que entra después de [after]: copia la más cercana de su tipo
 * hacia atrás (cifras, grupo, mazo), así nace con valores del mismo tablero; propiedad y tren se
 * llaman [name] y toman el dibujo de su nombre. Null si el tablero no tiene ninguna de ese tipo.
 */
fun newSquare(config: GameConfig, kind: NewKind, after: Int, name: String): Square? {
    val n = config.squares.size
    val near = (0 until n).map { config.squares[(after - it + n) % n] }
    return when (kind) {
        NewKind.PROPERTY -> near.firstNotNullOfOrNull { it as? Property }?.copy(name = name, art = null)
        NewKind.STATION -> near.firstNotNullOfOrNull { it as? Station }?.copy(name = name, art = null)
        NewKind.CARD -> near.firstNotNullOfOrNull { it as? CardSquare }
        NewKind.TAX -> near.firstNotNullOfOrNull { it as? Tax }
    }
}

/** Si la casilla [i] se puede quitar: la salida y la Cárcel no, porque no se reponen (D-41). */
fun removable(config: GameConfig, i: Int): Boolean = i > 0 && config.squares[i] !is Jail
