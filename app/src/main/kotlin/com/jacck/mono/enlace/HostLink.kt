package com.jacck.mono.enlace

import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import java.io.InputStream
import java.io.OutputStream

/**
 * Lado del anfitrión en el enlace (F5.3, D-46, D-47): lee una línea de JSON por mensaje de [input],
 * se la da a [host] y escribe sus respuestas en [output], hasta que el invitado cierra o dice `Bye`.
 * Una línea que no se entiende se cuenta y se salta (el invitado la pedirá de nuevo con `Resync`).
 * [onMessage] recibe cada mensaje entendido. No sabe de Bluetooth, para probarlo en la JVM.
 * Devuelve cuántas líneas leyó y cuántas no entendió.
 */
fun hostLoop(host: Host, input: InputStream, output: OutputStream, onMessage: (Message) -> Unit = {}): Pair<Int, Int> {
    val reader = input.bufferedReader(Charsets.UTF_8)
    val writer = output.bufferedWriter(Charsets.UTF_8)
    var read = 0
    var bad = 0
    while (true) {
        val line = reader.readLine() ?: break
        read++
        val message = try { decode(line) } catch (_: IllegalArgumentException) { bad++; continue }
        onMessage(message)
        if (message is Message.Bye) break
        // La pantalla también jugará con `host` (F5.4): un solo hilo a la vez.
        val replies = synchronized(host) { host.receive(message) }
        for (r in replies) writer.write(encode(r) + "\n")
        writer.flush()
    }
    return read to bad
}
