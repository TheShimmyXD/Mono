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
import com.jacck.mono.engine.model.TurnPhase

/**
 * La partida en curso (F3.3): guarda el estado del motor y le pasa cada acción (D-02). Un ViewModel
 * es la clase de Android que sobrevive a girar la pantalla; Compose redibuja al cambiar `state`.
 * La interfaz no decide reglas: si el motor rechaza la acción, se muestra su motivo en `error`.
 * Con `resumed` sigue una partida guardada; `onState` recibe cada estado nuevo para guardarlo (F3.6).
 * `tokens`: el personaje de cada jugador, escogido en el menú (FB.4, D-36).
 * Con `remote` la partida está enlazada con otro teléfono (F5.4, D-51): las jugadas de aquí van por
 * `remote.play`, las de allá llegan por `remote.listen`, y no se guarda ni se vuelve a empezar.
 */
class GameViewModel(
    val config: GameConfig,
    private val names: List<String>,
    seed: Long,
    private val prepare: (GameState) -> GameState = { it },
    resumed: GameState? = null,
    private val onState: (GameConfig, GameState) -> Unit = { _, _ -> },
    private val tokens: List<String> = emptyList(),
    val remote: Remote? = null,
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

    /** Número de la última jugada enlazada que se ve: una que llegue con número menor ya está incluida. */
    private var seen = 0

    /**
     * En una partida enlazada, el nombre del jugador del otro teléfono que tiene que decidir; null si
     * decide alguien de aquí (o no hay enlace). Mientras tanto la pantalla no ofrece botones.
     */
    val waitingFor: String?
        get() {
            val r = remote ?: return null
            if (state.phase is TurnPhase.Over) return null
            return decider(state).takeIf { it in r.seats }?.let { state.players[it].name }
        }

    init {
        onState(config, state)
        remote?.listen { n, result ->
            if (n > seen) {
                seen = n
                show(null, result)
            }
        }
    }

    /** La partida nueva; `prepare` reparte propiedades de prueba (extra `propiedades`, F3.4). */
    private fun start(seed: Long) = Engine.newGame(config, names, seed, tokens).let { it.copy(state = prepare(it.state)) }

    fun act(action: Action) {
        try {
            val result = remote?.play(action)?.let { (n, r) -> seen = n; r } ?: Engine.apply(config, state, action)
            show(action, result)
        } catch (e: IllegalActionException) {
            Log.w(LOG_TAG, "rechazada: $action (${e.message})")
            error = e.message
        } catch (e: IllegalArgumentException) { // Host.play: la decide el otro teléfono
            Log.w(LOG_TAG, "rechazada: $action (${e.message})")
            error = e.message
        }
    }

    /** Muestra lo que dejó una jugada; [action] es null si vino del otro teléfono. */
    private fun show(action: Action?, result: Result) {
        Log.i(LOG_TAG, "turno ${state.turn} · ${state.players[state.current].name}: ${action ?: "otro teléfono"} → ${result.events}")
        state = result.state
        onState(config, state)
        // Lo del otro teléfono no se vio aquí: se cuenta todo, no solo lo notable (D-51).
        if (action == null || result.events.any { it.isNotable() }) notices = notices + result.events
        result.events.filterIsInstance<Event.DiceRolled>().lastOrNull()?.let { lastDice = it.dice }
        error = null
    }

    fun dismissNotices() {
        notices = emptyList()
    }

    /** Otra partida con la misma configuración y los mismos jugadores. */
    fun restart(seed: Long) {
        if (remote != null) return
        val fresh = start(seed)
        state = fresh.state
        onState(config, state)
        notices = fresh.events
        lastDice = null
        error = null
    }

    override fun onCleared() {
        remote?.close()
    }
}
