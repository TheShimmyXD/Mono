package com.jacck.mono.enlace

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.PROTOCOL_VERSION
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class GuestLoopTest {

    @Test
    fun `saluda y se queda con la partida que manda el anfitrion`() {
        val config = Preset.CLASSIC.load()
        val host = Host(config, Engine.newGame(config, listOf("Ana", "Beto", "Caro"), seed = 7).state, setOf(1, 2))
        val guest = Guest("Redmi")
        val out = ByteArrayOutputStream()
        val read = guestLoop(guest, ByteArrayInputStream("basura\n${encode(host.snapshot())}\n".toByteArray()), out)
        assertEquals(2, read)
        assertEquals(listOf<Message>(Message.Hello(PROTOCOL_VERSION, "Redmi")), out.toString(Charsets.UTF_8).lines().filter { it.isNotEmpty() }.map(::decode))
        assertEquals(host.state, guest.state)
        assertEquals(setOf(1, 2), guest.seats)
        assertEquals(0, guest.last)
    }

    @Test
    fun `con Bye guarda el motivo y deja de leer`() {
        val guest = Guest("Redmi")
        val read = guestLoop(guest, ByteArrayInputStream("${encode(Message.Bye("versión"))}\notra\n".toByteArray()), ByteArrayOutputStream())
        assertEquals(1, read)
        assertEquals("versión", guest.closed)
    }
}
