package com.jacck.mono.enlace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class AcceptLoopTest {

    /** Abre [acceptLoop] en localhost con una sesión que lee hasta que se acaba la conexión. */
    private fun <T> room(silenceMs: Int, body: (port: Int, events: LinkedBlockingQueue<LinkEvent>) -> T): T {
        val events = LinkedBlockingQueue<LinkEvent>()
        val current = AtomicReference<Socket?>(null)
        var stopped = false
        ServerSocket(0).use { listening ->
            val loop = thread {
                acceptLoop(listening, current, { stopped }, events::put, { input, _ ->
                    val reader = input.bufferedReader()
                    while (reader.readLine() != null) {}
                }, silenceMs)
            }
            try {
                return body(listening.localPort, events)
            } finally {
                stopped = true
                listening.close()
                current.get()?.close()
                loop.join(2000)
            }
        }
    }

    private fun LinkedBlockingQueue<LinkEvent>.next(): LinkEvent = poll(3, TimeUnit.SECONDS) ?: error("sin evento en 3 s")

    @Test
    fun `F5_5 la conexion que vuelve reemplaza a la vieja sin contar un corte`() = room(silenceMs = 10_000) { port, events ->
        assertEquals(LinkEvent.Listening(port), events.next())
        Socket("127.0.0.1", port).use { old ->
            assertTrue((events.next() as LinkEvent.Connected).replaced.not())
            old.soTimeout = 3000
            Socket("127.0.0.1", port).use { back ->
                assertTrue((events.next() as LinkEvent.Connected).replaced, "la segunda reemplaza a la primera")
                assertEquals(-1, old.getInputStream().read(), "la sala cerró la conexión vieja")
                Thread.sleep(200)
                assertTrue(events.isEmpty(), "el fin de la vieja no cuenta como corte: ${events.toList()}")
                back.close()
                assertTrue(events.next() is LinkEvent.Closed)
                assertEquals(LinkEvent.Listening(port), events.next())
            }
        }
    }

    @Test
    fun `F5_5 un invitado callado mas de lo permitido es una red caida`() = room(silenceMs = 300) { port, events ->
        events.next()
        Socket("127.0.0.1", port).use {
            val start = System.nanoTime()
            assertTrue(events.next() is LinkEvent.Connected)
            val closed = events.next()
            val ms = (System.nanoTime() - start) / 1_000_000
            assertTrue(closed is LinkEvent.Closed, "$closed")
            assertTrue(ms in 250..2000, "se cortó a los $ms ms")
            assertEquals(LinkEvent.Listening(port), events.next(), "y vuelve a esperar")
        }
    }
}
