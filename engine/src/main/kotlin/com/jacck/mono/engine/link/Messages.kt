package com.jacck.mono.engine.link

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Protocolo de la partida entre dos teléfonos (F5.2, D-46; por la red local, D-48). El anfitrión tiene la partida de verdad:
 * aplica cada acción (suya o propuesta por el invitado), la numera y la envía; el invitado aplica
 * las mismas en orden y compara el resumen del estado. El azar va dentro de `GameState.random`
 * (D-03), así que los dados no viajan. Cada mensaje es una línea de JSON (`encode`/`decode`).
 */
const val PROTOCOL_VERSION = 1

@Serializable
sealed interface Message {

    /** Invitado → anfitrión, al conectar. */
    @Serializable
    @SerialName("hello")
    data class Hello(val version: Int, val name: String) : Message

    /** Anfitrión → invitado: la partida entera tras la acción `last` (al empezar o para reparar). */
    @Serializable
    @SerialName("snapshot")
    data class Snapshot(val version: Int, val config: GameConfig, val state: GameState, val seats: Set<Int>, val last: Int) : Message

    /** Invitado → anfitrión: «quiero hacer `action`», visto el estado tras la acción `after`. */
    @Serializable
    @SerialName("propose")
    data class Propose(val action: Action, val after: Int) : Message

    /** Anfitrión → invitado: la acción número `n` y el resumen del estado que dejó. */
    @Serializable
    @SerialName("applied")
    data class Applied(val n: Int, val action: Action, val digest: Long) : Message

    /** Anfitrión → invitado: la propuesta hecha tras `after` no vale. */
    @Serializable
    @SerialName("rejected")
    data class Rejected(val after: Int, val reason: String) : Message

    /** Invitado → anfitrión: «tengo hasta `after`»; con `full`, que mande la partida entera. */
    @Serializable
    @SerialName("resync")
    data class Resync(val after: Int, val full: Boolean = false) : Message

    /** Cualquiera: se acaba la conexión (p. ej. versiones distintas). */
    @Serializable
    @SerialName("bye")
    data class Bye(val reason: String) : Message
}

/** JSON compacto (sin saltos de línea): un mensaje por línea en el enlace. */
private val linkJson = Json { encodeDefaults = true }

fun encode(message: Message): String = linkJson.encodeToString(Message.serializer(), message)

fun decode(line: String): Message = linkJson.decodeFromString(Message.serializer(), line)

/**
 * Resumen del estado: FNV-1a de 64 bits sobre su JSON compacto. Los dos lados aplican las mismas
 * acciones con el mismo código, así que los mapas quedan en el mismo orden y el JSON es igual
 * carácter a carácter; un estado que llega en `Snapshot` conserva el orden con que se envió.
 */
fun digest(state: GameState): Long {
    var h = -0x340d631b7bdddcdbL // 0xcbf29ce484222325, base de FNV-1a
    for (c in linkJson.encodeToString(GameState.serializer(), state)) {
        h = (h xor c.code.toLong()) * 0x100000001b3L
    }
    return h
}

/** Quién decide `action` en `state`: el pujador en la subasta, el deudor en una deuda; si no, quien juega. */
fun actor(state: GameState, action: Action): Int = when {
    action is Action.Bid -> action.player
    action is Action.PassBid -> action.player
    action is Action.Trade -> action.from
    state.phase is TurnPhase.Debt -> (state.phase as TurnPhase.Debt).debts.first().debtor
    else -> state.current
}
