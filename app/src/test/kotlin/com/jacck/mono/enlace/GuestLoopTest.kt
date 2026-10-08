package com.jacck.mono.enlace

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.PROTOCOL_VERSION
import com.jacck.mono.engine.link.encode
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.game.Machine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.SocketTimeoutException
import kotlin.concurrent.thread
import kotlin.random.Random

class GuestLoopTest {

    private val classic = Preset.CLASSIC.load()

    @Test
    fun `saluda, se queda con la partida y si el anfitrion cierra devuelve el motivo`() {
        val host = Host(classic, Engine.newGame(classic, listOf("Ana", "Beto", "Caro"), seed = 7).state, setOf(1, 2))
        val guest = Guest("Redmi")
        val sent = mutableListOf<Message>()
        val reason = guestLoop(guest, ByteArrayInputStream("basura\n${encode(host.snapshot())}\n".toByteArray()), sent::add)
        assertEquals("el anfitrión cerró la conexión", reason)
        assertEquals(listOf<Message>(Message.Hello(PROTOCOL_VERSION, "Redmi")), sent)
        assertEquals(host.state, guest.state)
        assertEquals(setOf(1, 2), guest.seats)
        assertEquals(0, guest.last)
    }

    @Test
    fun `con Bye guarda el motivo, deja de leer y termina sin corte`() {
        val guest = Guest("Redmi")
        val reason = guestLoop(guest, ByteArrayInputStream("${encode(Message.Bye("versión"))}\n${encode(Message.Alive(0))}\n".toByteArray()), {})
        assertNull(reason)
        assertEquals("versión", guest.closed)
    }

    @Test
    fun `F5-10 dos silencios seguidos son un corte, y entre ellos pide lo que falta`() {
        val host = Host(classic, Engine.newGame(classic, listOf("Ana", "Beto"), seed = 7).state, setOf(1))
        val guest = Guest("Redmi")
        guest.receive(host.snapshot())
        val sent = mutableListOf<Message>()
        val silent = object : InputStream() {
            override fun read(): Int = throw SocketTimeoutException("Read timed out")
        }
        assertEquals("sin respuesta del anfitrión", guestLoop(guest, silent, sent::add))
        // Vuelve tras un corte (D-74): pide desde su última acción, y otra vez tras el primer silencio.
        assertEquals(listOf<Message>(Message.Resync(0), Message.Resync(0)), sent)
    }

    @Test
    fun `F5-10 mientras recibe manda un latido cada beatMs sin mandar nada`() {
        val host = Host(classic, Engine.newGame(classic, listOf("Ana", "Beto"), seed = 7).state, setOf(1))
        val lines = buildString {
            append(encode(host.snapshot()) + "\n")
            repeat(3) { append(encode(Message.Alive(0)) + "\n") }
        }
        var now = 0L
        val sent = mutableListOf<Message>()
        guestLoop(Guest("Redmi"), ByteArrayInputStream(lines.toByteArray()), sent::add, beatMs = 5_000) { now.also { now += 3_000 } }
        // Cada consulta del reloj avanza 3 s: tras la partida, uno de cada dos `Alive` cumple los 5 s sin mandar.
        assertEquals(listOf(Message.Hello(PROTOCOL_VERSION, "Redmi"), Message.Resync(0), Message.Resync(0)), sent)
    }

    @Test
    fun `F5-10 una partida corta entera contra hostLoop, con la maquina en los dos lados, acaba igual`() {
        val short = classic.copy(rules = classic.rules.copy(startingMoney = 300, salary = 0))
        val host = Host(short, Engine.newGame(short, listOf("Andrés", "Santi"), seed = 11).state, setOf(1))
        val guest = Guest("Redmi")
        val toHost = PipedOutputStream()
        val hostIn = PipedInputStream(toHost, 1 shl 16)
        val toGuest = PipedOutputStream()
        val guestIn = PipedInputStream(toGuest, 1 shl 16)
        val hostBox = Outbox(toGuest)
        val guestBox = Outbox(toHost)
        thread(name = "anfitrion") { try { hostLoop(host, hostIn, hostBox::send) } catch (_: IOException) {} } // al final se cierra el tubo
        var reason: String? = "sin terminar"
        val guestThread = thread(name = "invitado") { reason = guestLoop(guest, guestIn, guestBox::send) }
        val random = Random(5)
        val start = System.currentTimeMillis()
        var proposals = 0
        // La pantalla de cada lado: el anfitrión juega lo suyo y el invitado propone lo suyo.
        while (host.state.phase !is TurnPhase.Over && System.currentTimeMillis() - start < 60_000) {
            synchronized(host) { Machine.next(short, host.state, setOf(0), random)?.let { playLocal(host, it, hostBox::send) } }
            synchronized(guest) {
                val s = guest.state ?: return@synchronized
                Machine.next(short, s, guest.seats, random)?.let(guest::propose)?.let { guestBox.send(it); proposals++ }
            }
            Thread.sleep(1)
        }
        guestThread.join(10_000)
        hostBox.close()
        guestBox.close()
        assertTrue(host.state.phase is TurnPhase.Over, "sin terminar en la acción ${host.last}")
        assertNull(reason)
        assertEquals(0, guest.mismatches)
        assertEquals(host.last, guest.last)
        assertEquals(host.state, guest.state)
        assertTrue(proposals > 20, "$proposals propuestas")
        println("partida corta por tubos: ${host.last} acciones, $proposals propuestas del invitado, ${System.currentTimeMillis() - start} ms")
    }
}
