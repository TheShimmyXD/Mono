package com.jacck.mono.game

import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Tax
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** La ficha del editor de casillas (F4.1, D-39). */
class SquareDraftTest {

    private val cruces = Property("Las Cruces", "marron", 60, listOf(2, 10, 30, 90, 160, 250))
    private val artOf = { name: String -> "clave de $name" }

    @Test
    fun `sin cambios devuelve la misma casilla`() {
        assertEquals(cruces, SquareDraft.of(cruces).applyTo(cruces, artOf))
    }

    @Test
    fun `cambia nombre, precio, grupo y alquileres y guarda la clave del dibujo de antes`() {
        val draft = SquareDraft.of(cruces).copy(name = " La Candelaria ", price = "90", group = "celeste", rents = listOf("50", "10", "30", "90", "160", "250"))
        val edited = draft.applyTo(cruces, artOf) as Property
        assertEquals("La Candelaria", edited.name)
        assertEquals(90, edited.price)
        assertEquals("celeste", edited.group)
        assertEquals(50, edited.rents[0])
        assertEquals("clave de Las Cruces", edited.art)
    }

    @Test
    fun `una clave de dibujo ya puesta no se cambia al renombrar otra vez`() {
        val renamed = cruces.copy(name = "Otra", art = "las_cruces")
        assertEquals("las_cruces", SquareDraft.of(renamed).copy(name = "Tercera").applyTo(renamed, artOf)?.art)
    }

    @Test
    fun `una cifra vacia no guarda`() {
        assertNull(SquareDraft.of(cruces).copy(price = "").applyTo(cruces, artOf))
        assertNull(SquareDraft.of(cruces).copy(rents = listOf("", "10", "30", "90", "160", "250")).applyTo(cruces, artOf))
    }

    @Test
    fun `el impuesto edita su valor fijo y la carcel solo el nombre`() {
        assertEquals(150, (SquareDraft.of(Tax("Impuesto", 200)).copy(price = "150").applyTo(Tax("Impuesto", 200), artOf) as Tax).fixed)
        assertEquals(Jail("Calabozo", art = "clave de Cárcel"), SquareDraft("Calabozo").applyTo(Jail("Cárcel"), artOf))
    }
}
