package com.jacck.mono.enlace

import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.encode
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * Buzón de salida del enlace (F5.4): [send] deja el mensaje en una cola y vuelve enseguida; un hilo
 * propio los escribe en [output] en el mismo orden, una línea de JSON cada uno. Así la pantalla
 * puede mandar sus jugadas sin tocar la red (Android no deja usarla desde el hilo principal).
 * Si la escritura falla, el buzón se cierra solo y descarta lo demás: el invitado lo pedirá al volver.
 */
class Outbox(output: OutputStream) : AutoCloseable {

    private val queue = LinkedBlockingQueue<Any>()
    @Volatile private var closed = false

    /** Mensajes que ya salieron a la red (para el registro y las pruebas). */
    @Volatile var written = 0
        private set

    private val writer = thread(name = "mono-salida") {
        val out = output.bufferedWriter(Charsets.UTF_8)
        var buffered = 0
        try {
            while (true) {
                val next = queue.take()
                if (next === STOP) break
                out.write(encode(next as Message) + "\n")
                buffered++
                if (queue.isEmpty()) { out.flush(); written += buffered; buffered = 0 }
            }
            out.flush()
            written += buffered
        } catch (_: IOException) {
            closed = true
        } catch (_: InterruptedException) {
            closed = true
        }
    }

    fun send(message: Message) {
        if (!closed) queue.put(message)
    }

    /** Escribe lo que queda en la cola y termina el hilo (espera hasta 2 s). */
    override fun close() {
        if (closed) return
        closed = true
        queue.put(STOP)
        writer.join(2000)
    }

    private companion object {
        val STOP = Any()
    }
}
