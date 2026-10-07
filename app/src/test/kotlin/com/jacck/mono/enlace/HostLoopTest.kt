package com.jacck.mono.enlace

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.PROTOCOL_VERSION
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class HostLoopTest {

    private val config = Preset.CLASSIC.load()
    private fun host() = Host(config, Engine.newGame(config, listOf("Ana", "Beto", "Caro"), seed = 7).state, setOf(2))

    private fun run(host: Host, vararg lines: String): Triple<Pair<Int, Int>, List<Message>, List<Message>> {
        val out = ByteArrayOutputStream()
        val seen = mutableListOf<Message>()
        val counts = hostLoop(host, ByteArrayInputStream(lines.joinToString("") { "$it\n" }.toByteArray()), out) { seen += it }
        val sent = out.toString(Charsets.UTF_8).lines().filter { it.isNotEmpty() }.map(::decode)
        return Triple(counts, seen, sent)
    }

    @Test
    fun `al saludo responde la partida entera y a un pedido al dia no responde nada`() {
        val h = host()
        val (counts, seen, sent) = run(
            h, encode(Message.Hello(PROTOCOL_VERSION, "PC")), encode(Message.Resync(0)), "no es json", encode(Message.Resync(0, full = true)),
        )
        assertEquals(4 to 1, counts)
        assertEquals(3, seen.size)
        assertEquals(2, sent.size)
        sent.forEach { m ->
            assertTrue(m is Message.Snapshot)
            m as Message.Snapshot
            assertEquals(setOf(2), m.seats)
            assertEquals(0, m.last)
            assertEquals(h.state, m.state)
        }
    }

    @Test
    fun `con Bye deja de leer`() {
        val (counts, _, sent) = run(host(), encode(Message.Bye("me voy")), encode(Message.Hello(PROTOCOL_VERSION, "PC")))
        assertEquals(1 to 0, counts)
        assertEquals(emptyList<Message>(), sent)
    }

    @Test
    fun `otra version del protocolo recibe Bye`() {
        val (_, _, sent) = run(host(), encode(Message.Hello(PROTOCOL_VERSION + 1, "PC")))
        assertEquals(1, sent.size)
        assertTrue(sent.single() is Message.Bye)
    }

    @Test
    fun `el reloj de la sala va en minutos y segundos`() {
        assertEquals("0:00", clock(0))
        assertEquals("0:59", clock(59))
        assertEquals("10:05", clock(605))
    }
}
