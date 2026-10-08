package com.jacck.mono.game

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * La partida en curso (F3.3): guarda el estado del motor y le pasa cada acción (D-02). Un ViewModel
 * es la clase de Android que sobrevive a girar la pantalla; Compose redibuja al cambiar `state`.
 * La interfaz no decide reglas: si el motor rechaza la acción, se muestra su motivo en `error`.
 * Con `resumed` sigue una partida guardada; `onState` recibe cada estado nuevo para guardarlo (F3.6).
 * `tokens`: el personaje de cada jugador, escogido en el menú (FB.4, D-36).
 * Con `remote` la partida está enlazada con otro teléfono (F5.4, D-51): las jugadas de aquí van por
 * `remote.play`, las de allá llegan por `remote.listen`, y no se guarda ni se vuelve a empezar.
 * Los jugadores de `bots` los juega la máquina (F5.8b, D-55): cuando le toca, su jugada (`Machine`)
 * después de `pause` ms, y lo que hizo va entero a «Lo que pasó», como lo del otro teléfono.
 * Cada jugada deja en `walking` los recorridos de las fichas (FC.1, D-59); la pantalla los anima uno
 * a uno y avisa con `walked`; la máquina no juega mientras queden.
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
    val bots: Set<Int> = emptySet(),
    private val pause: Long = 900,
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

    /** Recorridos que la pantalla aún no ha animado, en orden (FC.1, D-59). */
    var walking: List<Walk> by mutableStateOf(emptyList())
        private set

    /** Cuántos recorridos ya se animaron: la pantalla anima el siguiente cuando cambia. */
    var walked: Int by mutableStateOf(0)
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

    /** El nombre del jugador de la máquina que tiene que decidir; null si decide una persona. */
    val machineTurn: String?
        get() = if (state.phase is TurnPhase.Over) null else decider(state).takeIf { it in bots }?.let { state.players[it].name }

    private val random = Random(seed)
    private var machine: Job? = null
    private var left = false

    init {
        onState(config, state)
        remote?.listen { n, result ->
            if (n > seen) {
                seen = n
                show(null, result)
            }
        }
        playMachine()
    }

    /** La partida nueva; `prepare` reparte propiedades de prueba (extra `propiedades`, F3.4). */
    private fun start(seed: Long) = Engine.newGame(config, names, seed, tokens).let { it.copy(state = prepare(it.state)) }

    fun act(action: Action) = play(action, byMachine = false)

    /** Si decide la máquina, su jugada después de la pausa; una a la vez. */
    private fun playMachine() {
        if (left || machineTurn == null || machine?.isActive == true) return
        machine = viewModelScope.launch {
            snapshotFlow { walking.isEmpty() }.first { it } // que la ficha llegue antes de pensar
            delay(pause)
            machine = null
            val action = Machine.next(config, state, bots, random)
            if (action != null) play(action, byMachine = true) else Log.w(LOG_TAG, "la máquina no tiene jugada en ${state.phase}")
        }
    }

    private fun play(action: Action, byMachine: Boolean) {
        try {
            val result = remote?.play(action)?.let { (n, r) -> seen = n; r } ?: Engine.apply(config, state, action)
            show(action, result, byMachine)
        } catch (e: IllegalActionException) {
            Log.w(LOG_TAG, "rechazada: $action (${e.message})")
            error = e.message
        } catch (e: IllegalArgumentException) { // Host.play: la decide el otro teléfono
            Log.w(LOG_TAG, "rechazada: $action (${e.message})")
            error = e.message
        }
    }

    /** Muestra lo que dejó una jugada; [action] es null si vino del otro teléfono. */
    private fun show(action: Action?, result: Result, byMachine: Boolean = false) {
        val who = if (byMachine) " (máquina)" else ""
        Log.i(LOG_TAG, "turno ${state.turn} · ${state.players[state.current].name}$who: ${action ?: "otro teléfono"} → ${result.events}")
        walking = walking + walks(result.events, config, state.players.map { it.position })
        state = result.state
        onState(config, state)
        // Lo del otro teléfono no se vio aquí: se cuenta todo, no solo lo notable (D-51).
        if (action == null || byMachine || result.events.any { it.isNotable() }) notices = notices + result.events
        result.events.filterIsInstance<Event.DiceRolled>().lastOrNull()?.let { lastDice = it.dice }
        error = null
        playMachine()
    }

    /** La pantalla terminó de animar el primer recorrido de `walking`. */
    fun walkDone() {
        walking = walking.drop(1)
        walked++
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
        walking = emptyList()
        lastDice = null
        error = null
        playMachine()
    }

    /** Se sale al menú (D-63): la máquina deja de jugar y el enlace se cierra. */
    fun leave() {
        left = true
        machine?.cancel()
        remote?.close()
    }

    override fun onCleared() {
        remote?.close()
    }
}
