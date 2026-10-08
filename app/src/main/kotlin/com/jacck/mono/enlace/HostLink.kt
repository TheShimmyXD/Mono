package com.jacck.mono.enlace

import com.jacck.mono.engine.Result
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.model.Action
import java.io.InputStream

/**
 * Lado del anfitrión en el enlace (F5.3, D-46, D-47): lee una línea de JSON por mensaje de [input],
 * se la da a [host] y manda sus respuestas con [send] (el [Outbox]), hasta que el invitado cierra o dice `Bye`.
 * Una línea que no se entiende se cuenta y se salta (el invitado la pedirá de nuevo con `Resync`).
 * [onMessage] recibe cada mensaje entendido y [onApplied], cada jugada del invitado que el anfitrión
 * aceptó, con su número, sus eventos y el estado que deja (F5.4). No sabe de sockets, para probarlo en la JVM.
 * Devuelve cuántas líneas leyó y cuántas no entendió.
 */
fun hostLoop(
    host: Host,
    input: InputStream,
    send: (Message) -> Unit,
    onMessage: (Message) -> Unit = {},
    onApplied: (Int, Result) -> Unit = { _, _ -> },
): Pair<Int, Int> {
    val reader = input.bufferedReader(Charsets.UTF_8)
    var read = 0
    var bad = 0
    while (true) {
        val line = reader.readLine() ?: break
        read++
        val message = try { decode(line) } catch (_: IllegalArgumentException) { bad++; continue }
        onMessage(message)
        if (message is Message.Bye) break
        // La pantalla también juega con `host` (HostRoom.play): un solo hilo a la vez, y lo que se
        // manda sale en el orden en que se aplicó.
        synchronized(host) {
            val before = host.last
            host.receive(message).forEach(send)
            if (host.last > before) onApplied(host.last, Result(host.state, host.events))
        }
    }
    return read to bad
}

/**
 * Una jugada de este teléfono en la partida enlazada (F5.4): la aplica [host] y su `Applied` sale por
 * [send], bajo el mismo candado que [hostLoop] para que el invitado las reciba en orden. Lanza
 * `IllegalActionException` si el motor no la acepta.
 */
fun playLocal(host: Host, action: Action, send: (Message) -> Unit): Result = synchronized(host) {
    send(host.play(action))
    Result(host.state, host.events)
}
