package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.MonoJson
import com.jacck.mono.engine.model.SavedGame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Guardar a mitad de partida y seguir igual (F3.6, D-26). */
class SavedGameTest {

    /** Juega `steps` acciones: la primera que el motor acepte entre tirar, comprar, multa y pasar. */
    private fun play(config: GameConfig, start: GameState, steps: Int): GameState {
        var state = start
        repeat(steps) {
            val next = listOf(Action.Roll, Action.Buy, Action.PayJailFine, Action.EndTurn)
                .firstNotNullOfOrNull { Engine.tryApply(config, state, it) } ?: return state
            state = next.state
        }
        return state
    }

    @Test
    fun `F3-6 guardada a mitad, sigue igual que sin guardar`() {
        for (preset in Preset.entries) {
            val config = preset.load()
            val middle = play(config, Engine.newGame(config, listOf("Ana", "Beto", "Caro"), 42).state, 40)
            assertTrue(middle.turn > 0 && middle.holdings.isNotEmpty(), "$preset: la partida avanzó")

            val back = MonoJson.decodeSaved(MonoJson.encodeSaved(SavedGame(config, middle)))
            assertEquals(SavedGame(config, middle), back)
            assertEquals(play(config, middle, 40), play(back.config, back.state, 40), "$preset: mismas 40 jugadas después")
        }
    }

    @Test
    fun `FB-4 el personaje de cada jugador se guarda con la partida`() {
        val config = Preset.CLASSIC.load()
        val state = Engine.newGame(config, listOf("Ana", "Beto", "Caro"), 7, tokens = listOf("arepa", "mono", "tinto")).state
        val back = MonoJson.decodeSaved(MonoJson.encodeSaved(SavedGame(config, state))).state
        assertEquals(listOf("arepa", "mono", "tinto"), back.players.map { it.token })
    }

    @Test
    fun `FB-4 una partida guardada antes de los personajes carga sin ellos`() {
        val config = Preset.CLASSIC.load()
        val state = Engine.newGame(config, listOf("Ana", "Beto"), 7, tokens = listOf("arepa", "mono")).state
        val json = MonoJson.encodeSaved(SavedGame(config, state))
        val old = json.replace(Regex(",\\s*\"token\":\\s*\"[a-z]+\""), "")
        assertTrue("\"token\"" !in old, "el JSON viejo no trae personajes")
        val back = MonoJson.decodeSaved(old).state
        back.players.forEach { assertNull(it.token) }
        assertEquals(state.players.map { it.copy(token = null) }, back.players)
    }
}
