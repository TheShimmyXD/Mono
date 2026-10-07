package com.jacck.mono.game

/** Largo máximo de un nombre: cabe en la ficha del panel y en los diálogos (D-25). */
const val MAX_NAME = 12

/**
 * Los nombres con que empieza la partida (F3.5, D-25): sin espacios en los bordes y, si la casilla
 * quedó vacía, el nombre de muestra de esa posición (Ana, Beto…). `typed` y `defaults` van en el
 * mismo orden; se toman los `count` primeros.
 */
fun playerNames(typed: List<String>, defaults: List<String>, count: Int): List<String> =
    (0 until count).map { i -> typed.getOrElse(i) { "" }.trim().take(MAX_NAME).trim().ifEmpty { defaults[i] } }

/** Posiciones cuyo nombre se repite (sin distinguir mayúsculas): el menú no deja empezar así. */
fun repeatedNames(names: List<String>): Set<Int> {
    val keys = names.map { it.lowercase() }
    return keys.indices.filter { i -> keys.count { it == keys[i] } > 1 }.toSet()
}
