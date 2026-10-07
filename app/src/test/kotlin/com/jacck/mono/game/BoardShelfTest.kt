package com.jacck.mono.game

import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.withSquareRemoved
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** Tableros propios guardados en archivos (F4.4, D-42). */
class BoardShelfTest {

    private val classic = Preset.CLASSIC.load()

    @Test
    fun `sin carpeta no hay tableros y solo salen los originales`(@TempDir dir: File) {
        assertEquals(emptyList<OwnBoard>(), BoardShelf(dir).list())
        assertEquals(listOf("CLASSIC", "TIO_RICO"), boardChoices(emptyList()).map { it.key })
    }

    @Test
    fun `un tablero guardado sobrevive a abrir la estanteria de nuevo`(@TempDir dir: File) {
        val small = classic.withSquareRemoved(39).withSquareRemoved(38).copy(name = "Clásico de 38")
        val id = BoardShelf(dir).save(small)
        assertEquals(listOf(OwnBoard(id, small)), BoardShelf(dir).list())
        assertEquals(listOf("$id.json"), File(dir, "tableros").list()!!.toList(), "sin temporales sueltos")
    }

    @Test
    fun `guardar con id sobrescribe y sin id crea otro en orden`(@TempDir dir: File) {
        val shelf = BoardShelf(dir)
        val a = shelf.save(classic.copy(name = "A"))
        val b = shelf.save(classic.copy(name = "B"))
        shelf.save(classic.copy(name = "A2"), a)
        assertEquals(listOf("A2", "B"), shelf.list().map { it.config.name })
        shelf.delete(a)
        assertEquals(listOf(b), shelf.list().map { it.id })
        assertTrue(shelf.save(classic) != b, "uno nuevo no pisa a otro")
    }

    @Test
    fun `un archivo danado se salta`(@TempDir dir: File) {
        val shelf = BoardShelf(dir)
        val ok = shelf.save(classic)
        File(dir, "tableros/t9.json").writeText("{ roto")
        assertEquals(listOf(ok), shelf.list().map { it.id })
    }

    @Test
    fun `la copia lleva copia y un numero si ya esta`() {
        assertEquals("Clásico copia", copyName("Clásico", listOf("Clásico", "Tío Rico")))
        assertEquals("Clásico copia 2", copyName("Clásico", listOf("clásico COPIA")))
        assertEquals(MAX_BOARD_NAME, copyName("Un nombre bastante largo", emptyList()).length)
    }

    @Test
    fun `el nombre vacio deja el de antes`() {
        assertEquals("Mi mapa", boardName("  Mi mapa ", "Clásico copia"))
        assertEquals("Clásico copia", boardName("   ", "Clásico copia"))
    }
}
