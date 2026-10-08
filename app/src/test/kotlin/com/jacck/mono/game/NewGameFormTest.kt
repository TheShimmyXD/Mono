package com.jacck.mono.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Nombres del menú de nueva partida (F3.5, D-25). */
class NewGameFormTest {

    private val defaults = listOf("Ana", "Beto", "Caro", "Dani", "Eva", "Fede")

    @Test
    fun `vacios toman el nombre de muestra y se recortan los espacios`() {
        assertEquals(listOf("Jacck", "Beto", "Luz"), playerNames(listOf("  Jacck ", "   ", "Luz", "Sobra"), defaults, 3))
    }

    @Test
    fun `faltan casillas, se completan con los de muestra`() {
        assertEquals(listOf("Ana", "Beto"), playerNames(emptyList(), defaults, 2))
    }

    @Test
    fun `un nombre largo se corta en MAX_NAME`() {
        assertEquals("Maximiliano", playerNames(listOf("Maximiliano Rojas"), defaults, 1)[0])
        assertEquals(MAX_NAME, playerNames(listOf("Bartolomeo Ruiz"), defaults, 1)[0].length)
    }

    @Test
    fun `repetidos sin distinguir mayusculas`() {
        assertEquals(setOf(0, 2), repeatedNames(listOf("Ana", "Beto", "ana")))
        assertEquals(emptySet<Int>(), repeatedNames(listOf("Ana", "Beto", "Caro")))
    }

    @Test
    fun `vacio que cae en un nombre ya escrito cuenta como repetido`() {
        val names = playerNames(listOf("", "Ana"), defaults, 2)
        assertEquals(setOf(0, 1), repeatedNames(names))
    }

    @Test
    fun `D-57 los de la maquina se llaman Botty 1, Botty 2 en orden, sin importar lo escrito`() {
        assertEquals(listOf("Jacck", "Botty 1", "Caro", "Botty 2"), playerNames(listOf("Jacck", "Luz", ""), defaults, 4, setOf(1, 3)))
    }

    @Test
    fun `D-57 el menu abre con 2 y llega hasta 4`() {
        assertEquals(2, DEFAULT_PLAYERS)
        assertEquals(4, MAX_PLAYERS)
    }

    @Test
    fun `FB-4 por defecto cada jugador tiene el personaje de su posicion`() {
        assertEquals(listOf(0, 1, 2), playerTokens(List(6) { it }, 3, 8))
    }

    @Test
    fun `FB-4 lo escogido se respeta y no se repite`() {
        assertEquals(listOf(5, 1, 0), playerTokens(listOf(5, 1, 0, 3, 4, 5), 3, 8))
        // Ana escogió el 3 con 3 jugadores; al subir a 4, Dani (que tenía el 3) toma el primero libre.
        assertEquals(listOf(3, 1, 2, 0), playerTokens(listOf(3, 1, 2, 3, 4, 5), 4, 8))
    }
}
