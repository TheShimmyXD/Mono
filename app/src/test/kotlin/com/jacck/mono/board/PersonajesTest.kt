package com.jacck.mono.board

import com.jacck.mono.R
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** El dibujo de cada ficha (FB.4, D-36). */
class PersonajesTest {

    @Test
    fun `FB-4 el personaje escogido se busca por su id`() {
        assertEquals(R.drawable.pj_arepa, personaje("arepa", 0))
        assertEquals(R.drawable.pj_guacamaya, personaje("guacamaya", 5))
    }

    @Test
    fun `FB-4 sin personaje o con uno desconocido, el de su posicion`() {
        assertEquals(R.drawable.pj_mono, personaje(null, 0))
        assertEquals(R.drawable.pj_sombrero, personaje(null, 2))
        assertEquals(R.drawable.pj_chiva, personaje("tigre", 1))
    }

    @Test
    fun `FB-4 alcanzan para 6 jugadores sin repetir`() {
        assertEquals(8, Personajes.map { it.first }.distinct().size)
        assertEquals(6, (0 until 6).map { personaje(null, it) }.distinct().size)
    }
}
