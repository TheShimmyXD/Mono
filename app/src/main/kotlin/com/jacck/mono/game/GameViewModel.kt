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
import com.jacck.mono.engine.Result
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState

/**
 * La partida en curso (F3.3): guarda el estado del motor y le pasa cada acción (D-02). Un ViewModel
 * es la clase de Android que sobrevive a girar la pantalla; Compose redibuja al cambiar `state`.
 * La interfaz no decide reglas: si el motor rechaza la acción, se muestra su motivo en `error`.
 * Con `resumed` sigue una partida guardada; `onState` recibe cada estado nuevo para guardarlo (F3.6).
 * `tokens`: el personaje de cada jugador, escogido en el menú (FB.4, D-36).
 */
class GameViewModel(
    val config: GameConfig,
    private val names: List<String>,
    seed: Long,
    private val prepare: (GameState) -> GameState = { it },
    resumed: GameState? = null,
    private val onState: (GameConfig, GameState) -> Unit = { _, _ -> },
    private val tokens: List<String> = emptyList(),
) : ViewModel() {

    private val first = resumed?.let { Result(it, emptyList()) } ?: start(seed)

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

    init {
        onState(config, state)
    }

    /** La partida nueva; `prepare` reparte propiedades de prueba (extra `propiedades`, F3.4). */
    private fun start(seed: Long) = Engine.newGame(config, names, seed, tokens).let { it.copy(state = prepare(it.state)) }

    fun act(action: Action) {
        try {
            val result = Engine.apply(config, state, action)
            Log.i(LOG_TAG, "turno ${state.turn} · ${state.players[state.current].name}: $action → ${result.events}")
            state = result.state
            onState(config, state)
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
        onState(config, state)
        notices = fresh.events
        lastDice = null
        error = null
    }
}
