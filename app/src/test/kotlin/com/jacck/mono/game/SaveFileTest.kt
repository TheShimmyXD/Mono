package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.SavedGame
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** Archivo de la partida guardada (F3.6, D-26). */
class SaveFileTest {

    private val config = Preset.CLASSIC.load()
    private val start = Engine.newGame(config, listOf("Ana", "Beto", "Caro"), 42).state

    @Test
    fun `sin archivo no hay partida`(@TempDir dir: File) {
        assertNull(SaveFile(dir).read())
    }

    @Test
    fun `lo escrito se lee igual y la ultima escritura gana`(@TempDir dir: File) {
        val rolled = Engine.apply(config, start, Action.Roll).state
        SaveFile(dir).write(SavedGame(config, start))
        SaveFile(dir).write(SavedGame(config, rolled))
        assertEquals(SavedGame(config, rolled), SaveFile(dir).read())
        assertEquals(listOf("partida.json"), dir.list()!!.toList(), "sin temporales sueltos")
    }

    @Test
    fun `una partida terminada borra el archivo`(@TempDir dir: File) {
        SaveFile(dir).write(SavedGame(config, start))
        SaveFile(dir).write(SavedGame(config, start.copy(phase = TurnPhase.Over(listOf(0)))))
        assertNull(SaveFile(dir).read())
        assertEquals(0, dir.list()!!.size)
    }

    @Test
    fun `un archivo danado no rompe la app`(@TempDir dir: File) {
        File(dir, "partida.json").writeText("{\"config\": 1")
        assertNull(SaveFile(dir).read())
    }
}
