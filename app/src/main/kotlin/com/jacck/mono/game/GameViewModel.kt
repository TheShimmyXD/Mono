package com.jacck.mono.game

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.jacck.mono.LOG_TAG
import com.jacck.mono.engine.Dice
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.IllegalActionException
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState

/**
 * La partida en curso (F3.3): guarda el estado del motor y le pasa cada acción (D-02). Un ViewModel
 * es la clase de Android que sobrevive a girar la pantalla; Compose redibuja al cambiar `state`.
 * La interfaz no decide reglas: si el motor rechaza la acción, se muestra su motivo en `error`.
 */
class GameViewModel(val config: GameConfig, private val names: List<String>, seed: Long) : ViewModel() {

    private val first = start(seed)

    var state: GameState by mutableStateOf(first.state)
        private set

    /** Lo que pasó y aún no se ha mostrado; solo si hubo algo notable (`isNotable`, D-22). */
    var notices: List<Event> by mutableStateOf(first.events)
        private set

    /** Últimos dados tirados por alguien, para el centro del tablero. */
    var lastDice: Dice? by mutableStateOf(null)
        private set

    var error: String? by mutableStateOf(null)
        private set

    private fun start(seed: Long) = Engine.newGame(config, names, seed)

    fun act(action: Action) {
        try {
            val result = Engine.apply(config, state, action)
            Log.i(LOG_TAG, "turno ${state.turn} · ${state.players[state.current].name}: $action → ${result.events}")
            state = result.state
            if (result.events.any { it.isNotable() }) notices = notices + result.events
            result.events.filterIsInstance<Event.DiceRolled>().lastOrNull()?.let { lastDice = it.dice }
            error = null
        } catch (e: IllegalActionException) {
            Log.w(LOG_TAG, "rechazada: $action (${e.message})")
            error = e.message
        }
    }

    fun dismissNotices() {
        notices = emptyList()
    }

    /** Otra partida con la misma configuración y los mismos jugadores. */
    fun restart(seed: Long) {
        val fresh = start(seed)
        state = fresh.state
        notices = fresh.events
        lastDice = null
        error = null
    }
}
