package com.jacck.mono.game

import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility

/** Cifras de hasta 5 dígitos en los campos del editor (el validador pone el tope real, D-09). */
const val MAX_DIGITS = 5

/**
 * Lo escrito en la ficha del editor de casillas (F4.1, D-39), como texto: [price] es el precio de
 * las que se compran y el valor fijo de un impuesto; [rents], los alquileres de una propiedad
 * (solar … hotel) o de un ferrocarril (1 … 4); [group], el grupo de una propiedad.
 */
data class SquareDraft(val name: String, val price: String = "", val group: String? = null, val rents: List<String> = emptyList()) {

    /**
     * La casilla [sq] con lo escrito, o null si falta una cifra. Al renombrar una casilla sin clave
     * de dibujo le guarda la de su nombre de antes ([artOf]), para que no pierda su arte (D-39).
     */
    fun applyTo(sq: Square, artOf: (String) -> String): Square? {
        val name = name.trim()
        val art = sq.art ?: artOf(sq.name).takeIf { name != sq.name }
        val price = price.toIntOrNull()
        val rents = rents.map { it.toIntOrNull() ?: return null }
        return when (sq) {
            is Property -> sq.copy(name = name, price = price ?: return null, group = group ?: sq.group, rents = rents, art = art)
            is Station -> sq.copy(name = name, price = price ?: return null, rents = rents, art = art)
            is Utility -> sq.copy(name = name, price = price ?: return null, art = art)
            is Tax -> sq.copy(name = name, fixed = price ?: return null, art = art)
            is Start -> sq.copy(name = name, art = art)
            is CardSquare -> sq.copy(name = name, art = art)
            is Jail -> sq.copy(name = name, art = art)
            is GoToJail -> sq.copy(name = name, art = art)
            is Rest -> sq.copy(name = name, art = art)
        }
    }

    companion object {
        /** La ficha llena con lo que tiene la casilla. */
        fun of(sq: Square): SquareDraft = when (sq) {
            is Property -> SquareDraft(sq.name, "${sq.price}", sq.group, sq.rents.map { "$it" })
            is Station -> SquareDraft(sq.name, "${sq.price}", rents = sq.rents.map { "$it" })
            is Utility -> SquareDraft(sq.name, "${sq.price}")
            is Tax -> SquareDraft(sq.name, "${sq.fixed}")
            else -> SquareDraft(sq.name)
        }
    }
}
