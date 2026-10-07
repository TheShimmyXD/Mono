package com.jacck.mono.board

import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Utility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Qué ícono lleva cada casilla de los dos presets (FA.2, D-29). */
class IconTest {

    private val classic = Preset.CLASSIC.load().squares
    private val tioRico = Preset.TIO_RICO.load().squares

    @Test
    fun `toda casilla que no es propiedad tiene icono y las propiedades no`() {
        for (sq in classic + tioRico) {
            if (sq is Property) assertNull(sq.icon(), sq.name) else assertEquals(true, sq.icon() != null, sq.name)
        }
    }

    @Test
    fun `el Clasico distingue energia y acueducto, y sus dos mazos`() {
        assertEquals(setOf(Icon.ENERGIA, Icon.ACUEDUCTO), classic.filterIsInstance<Utility>().map { it.icon() }.toSet())
        assertEquals(setOf(Icon.CASUALIDAD, Icon.ARCA), classic.filterIsInstance<CardSquare>().map { it.icon() }.toSet())
        assertEquals(setOf(Icon.PARADA_LIBRE), classic.filterIsInstance<Rest>().map { it.icon() }.toSet())
    }

    @Test
    fun `Tio Rico tiene Loteria, Sorpresa, Mirador y Hamaca`() {
        assertEquals(setOf(Icon.LOTERIA, Icon.SORPRESA), tioRico.filterIsInstance<CardSquare>().map { it.icon() }.toSet())
        assertEquals(setOf(Icon.MIRADOR, Icon.HAMACA), tioRico.filterIsInstance<Rest>().map { it.icon() }.toSet())
    }

    @Test
    fun `un nombre que no se reconoce cae en el icono general del tipo`() {
        assertEquals(Icon.CASUALIDAD, CardSquare("Mi mazo", Deck.A).icon())
        assertEquals(Icon.ARCA, CardSquare("Otro", Deck.B).icon())
        assertEquals(Icon.PARADA_LIBRE, Rest("Descanso").icon())
        assertEquals(Icon.ENERGIA, Utility("Gas", 150, listOf(4, 10)).icon())
    }

    @Test
    fun `las seis caras del dado`() {
        assertEquals(listOf(Icon.DADO_1, Icon.DADO_2, Icon.DADO_3, Icon.DADO_4, Icon.DADO_5, Icon.DADO_6), (1..6).map { Icon.dado(it) })
    }
}

/** Clave del arte de cada lugar (FA.3, D-30): igual que el id de `arte/arte.py`. */
class ArtKeyTest {

    @Test
    fun `los lugares con arte se encuentran por su nombre`() {
        assertEquals("villa_de_leyva", artKey("Villa de Leyva"))
        assertEquals("san_andres", artKey("San Andrés"))
        assertEquals("barrio_egipto", artKey(" Barrio  Egipto "))
        val names = (Preset.CLASSIC.load().squares + Preset.TIO_RICO.load().squares).map { artKey(it.name) }.toSet()
        assertEquals(ArteLugares.keys, ArteLugares.keys.filter { it in names }.toSet())
    }
}
