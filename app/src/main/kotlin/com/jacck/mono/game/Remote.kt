package com.jacck.mono.game

import com.jacck.mono.engine.Result
import com.jacck.mono.engine.model.Action

/**
 * El otro lado de una partida enlazada (F5.4, D-51), visto desde [GameViewModel]: la partida de
 * verdad está allí (el `Host` de la sala) y cada jugada lleva su número, para no volver atrás si
 * una del otro teléfono llega después de una de este. Lo implementan `enlace/HostRoom.kt` (la sala) y
 * `enlace/GuestRoom.kt` (el invitado, F5.10, D-76).
 */
interface Remote : AutoCloseable {
    /** Jugadores que se manejan en el otro teléfono. */
    val seats: Set<Int>

    /** Hay un invitado conectado ahora mismo. */
    val connected: Boolean

    /**
     * Aplica una jugada de este teléfono; devuelve su número y lo que dejó, o null si solo se propuso
     * y vuelve aplicada por [listen] (el invitado). Lanza `IllegalActionException`.
     */
    fun play(action: Action): Pair<Int, Result>?

    /** [onApplied] recibe, en el hilo principal, cada jugada del otro teléfono con su número. */
    fun listen(onApplied: (Int, Result) -> Unit)
}
