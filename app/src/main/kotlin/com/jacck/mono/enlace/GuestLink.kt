package com.jacck.mono.enlace

import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import java.io.InputStream
import java.io.OutputStream

/**
 * Lado del invitado en el enlace (F5.3, D-46, D-47): saluda con `Hello`, le da a [guest] cada línea
 * de JSON de [input] y escribe sus respuestas en [output], hasta que el anfitrión cierra o dice
 * `Bye`. Una línea que no se entiende se salta. [onMessage] recibe cada mensaje entendido, ya
 * aplicado. No sabe de Bluetooth, para probarlo en la JVM. Devuelve cuántas líneas leyó.
 */
fun guestLoop(guest: Guest, input: InputStream, output: OutputStream, onMessage: (Message) -> Unit = {}): Int {
    val reader = input.bufferedReader(Charsets.UTF_8)
    val writer = output.bufferedWriter(Charsets.UTF_8)
    writer.write(encode(guest.hello()) + "\n")
    writer.flush()
    var read = 0
    while (true) {
        val line = reader.readLine() ?: break
        read++
        val message = try { decode(line) } catch (_: IllegalArgumentException) { continue }
        // La pantalla también propondrá con `guest` (F5.4): un solo hilo a la vez.
        val replies = synchronized(guest) { guest.receive(message) }
        onMessage(message)
        if (message is Message.Bye) break
        for (r in replies) writer.write(encode(r) + "\n")
        writer.flush()
    }
    return read
}
