package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.ZoneId

/** El historial (FH.2, D-92): `historial.json`, el tiempo jugado y cómo se muestra. */
class HistoryTest {

    @TempDir
    lateinit var dir: File

    private val config = Preset.CLASSIC.load()
    private val start = Engine.newGame(config, listOf("Ana", "Beto", "Caro"), seed = 1).state
    private var now = 0L
    private fun keeper(file: HistoryFile, open: Boolean = true) =
        HistoryKeeper(file, file.open() ?: newRecord(config, start, 1000, setOf(1), setOf(2), open).also(file::add), { now }, {})

    @Test
    fun `se agrega y se lee, con maquina y otro telefono marcados`() {
        val file = HistoryFile(dir)
        assertEquals(emptyList<Any>(), file.read())
        keeper(file)
        val r = file.read().single()
        assertEquals(listOf("Ana", "Beto", "Caro"), r.players.map { it.name })
        assertEquals(listOf(false, true, false), r.players.map { it.machine })
        assertEquals(listOf(false, false, true), r.players.map { it.remote })
        assertEquals(config.name, r.board)
        assertTrue(r.open)
        assertNull(r.winners)
    }

    @Test
    fun `el tiempo se suma entre dos sesiones, sin contar la pausa`() {
        val file = HistoryFile(dir)
        val a = keeper(file)
        a.start()
        now = 600_000 // 10 min jugando
        a.stop(config, start.copy(turn = 20))
        now = 5_000_000 // la app cerrada: no cuenta
        val b = keeper(file) // «Seguir la partida»
        b.start()
        now += 300_000 // 5 min más
        b.update(config, start.copy(turn = 31))
        val r = file.read().single()
        assertEquals(900_000, r.playedMs)
        assertEquals(31, r.turns)
    }

    @Test
    fun `al terminar quedan los ganadores y ya no cambia`() {
        val file = HistoryFile(dir)
        val k = keeper(file)
        k.start()
        now = 60_000
        k.update(config, start.copy(turn = 40, phase = TurnPhase.Over(listOf(2))))
        now = 120_000
        k.stop(config, start.copy(turn = 40, phase = TurnPhase.Over(listOf(2))))
        val r = file.read().single()
        assertEquals(listOf(2), r.winners)
        assertEquals(config.rules.endCondition, r.end)
        assertEquals(60_000, r.playedMs)
        assertFalse(r.open)
    }

    @Test
    fun `una partida nueva deja sin terminar la abierta, y otra partida empieza registro`() {
        val file = HistoryFile(dir)
        keeper(file)
        val k = HistoryKeeper(file, newRecord(config, start, 2000, emptySet(), emptySet(), open = true).also(file::add), { now }, {})
        assertEquals(listOf(false, true), file.read().map { it.open })
        k.update(config, start.copy(turn = 12, phase = TurnPhase.Over(listOf(0))))
        k.restart(start, 3000)
        val all = file.read()
        assertEquals(listOf(1000L, 2000L, 3000L), all.map { it.started })
        assertEquals(listOf(0), all[1].winners)
        assertNull(all[2].winners)
        assertTrue(all[2].open)
    }

    @Test
    fun `un archivo danado no rompe el menu, y se aparta`() {
        File(dir, "historial.json").writeText("{ esto no es un historial")
        val file = HistoryFile(dir)
        assertEquals(emptyList<Any>(), file.read())
        assertTrue(File(dir, "historial_danado.json").exists())
        keeper(file)
        assertEquals(1, file.read().size)
    }

    @Test
    fun `duracion y fecha`() {
        assertEquals(1L to 12L, hoursMinutes(72 * 60_000L + 59_000))
        assertEquals(0L to 48L, hoursMinutes(48 * 60_000L))
        // 2026-10-11 es domingo (0); octubre es el mes 9.
        assertEquals(Triple(0, 11, 9), dayMonth(1_791_720_000_000, ZoneId.of("America/Bogota")))
    }
}
