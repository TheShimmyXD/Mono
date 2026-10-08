package com.jacck.mono.enlace

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.actor
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.digest
import com.jacck.mono.engine.link.encode
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class OutboxTest {

    @Test
    fun `send vuelve enseguida y el hilo escribe todo en orden`() {
        val out = ByteArrayOutputStream()
        // Una red lenta: 5 ms por cada escritura.
        val slow = object : OutputStream() {
            override fun write(b: Int) = out.write(b)
            override fun write(b: ByteArray, off: Int, len: Int) { Thread.sleep(5); out.write(b, off, len) }
        }
        val box = Outbox(slow)
        repeat(50) { box.send(Message.Resync(it)) }
        assertTrue(box.written < 50, "send no espera a la red: ${box.written} escritos al volver")
        box.close()
        assertEquals(50, box.written)
        val sent = out.toString(Charsets.UTF_8).lines().filter { it.isNotEmpty() }.map { (decode(it) as Message.Resync).after }
        assertEquals((0 until 50).toList(), sent)
    }

    @Test
    fun `si la red falla el buzon se cierra sin lanzar y descarta lo demas`() {
        val broken = object : OutputStream() {
            override fun write(b: Int) = throw IOException("sin red")
            override fun write(b: ByteArray, off: Int, len: Int) = throw IOException("sin red")
        }
        val box = Outbox(broken)
        repeat(10) { box.send(Message.Resync(it)) }
        box.close()
        box.send(Message.Resync(99))
        assertEquals(0, box.written)
    }

    /** Primera jugada que el motor acepta de [player], de una lista corta que alcanza para terminar turnos y pagar deudas. */
    private fun pick(config: GameConfig, state: GameState, player: Int): Action? {
        val owned = state.holdings.filterValues { it.owner == player }.keys
        return (listOf(Action.Roll, Action.Buy, Action.Decline, Action.PayTax(false), Action.EndTurn, Action.PassBid(player)) +
            owned.map { Action.SellBuilding(it) } + owned.map { Action.Mortgage(it) } + Action.DeclareBankruptcy)
            .firstOrNull { actor(state, it) == player && Engine.tryApply(config, state, it) != null }
    }

    @Test
    fun `F5-4 jugadas del telefono y del invitado a la vez por localhost acaban iguales`() {
        val config = Preset.CLASSIC.load()
        val host = Host(config, Engine.newGame(config, listOf("Ana", "PC"), seed = 11).state, setOf(1))
        val guest = Guest("PC")
        val remote = mutableListOf<Int>()
        val outbox = AtomicReference<Outbox?>(null)
        val server = ServerSocket(0, 1, InetAddress.getLoopbackAddress())
        val hostSide = thread(name = "anfitrion") {
            server.accept().use { s ->
                Outbox(s.getOutputStream()).use { box ->
                    outbox.set(box)
                    hostLoop(host, s.getInputStream(), box::send, onApplied = { n, _ -> remote += n })
                }
            }
        }
        val socket = Socket(server.inetAddress, server.localPort)
        val writer = socket.getOutputStream().bufferedWriter()
        fun send(m: List<Message>) { m.forEach { writer.write(encode(it) + "\n") }; writer.flush() }
        val guestSide = thread(name = "invitado") {
            send(listOf(guest.hello()))
            // Como el PC: si no llega nada en 500 ms, pide lo que falta (una propuesta que cruzó con
            // una jugada del teléfono se queda sin respuesta).
            socket.soTimeout = 500
            val reader = socket.getInputStream().bufferedReader()
            while (true) {
                val line = try { reader.readLine() ?: break } catch (_: SocketTimeoutException) { null }
                val replies = if (line == null) guest.timeout() else guest.receive(decode(line))
                val s = guest.state
                val mine = s?.let { st -> guest.seats.firstNotNullOfOrNull { p -> pick(config, st, p) } }
                send(replies + listOfNotNull(mine?.let(guest::propose)))
            }
        }
        // El teléfono juega lo suyo desde este hilo, como la pantalla, mientras el invitado juega lo de él.
        var local = 0
        val deadline = System.currentTimeMillis() + 20_000
        while (host.last < 400 && host.state.phase !is TurnPhase.Over && System.currentTimeMillis() < deadline) {
            val action = synchronized(host) { pick(config, host.state, 0) }
            if (action != null) { playLocal(host, action) { outbox.get()?.send(it) }; local++ } else Thread.sleep(1)
        }
        while (guest.last != host.last && System.currentTimeMillis() < deadline) Thread.sleep(5)
        socket.shutdownOutput()
        hostSide.join(5000)
        socket.close()
        guestSide.join(5000)
        server.close()
        println("F5-4 por localhost: ${host.last} jugadas, $local del teléfono y ${remote.size} del invitado, ${host.state.phase::class.simpleName}, ${guest.mismatches} resúmenes distintos")
        assertTrue(host.last >= 400 || host.state.phase is TurnPhase.Over, "partida corta: ${host.last}, ${host.state.phase}, turno de ${host.state.current}, dinero ${host.state.players.map { it.money }}, presos ${host.state.players.map { it.jailTurns }}")
        assertTrue(local > 0 && remote.isNotEmpty(), "jugaron los dos: $local del teléfono, ${remote.size} del invitado")
        assertEquals(host.last, guest.last)
        assertEquals(digest(host.state), digest(guest.state!!))
        assertEquals(0, guest.mismatches)
    }
}
