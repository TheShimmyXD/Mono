package com.jacck.mono.terminal

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.digest
import com.jacck.mono.engine.link.encode
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

class TerminalTest {

    /**
     * El Clásico con $300 al empezar y sin salario: sin tratos casi nadie completa un grupo (D-43) y con
     * el salario la partida no acaba (más de 20 000 acciones); sin él, el dinero solo se va.
     */
    private val short = Preset.CLASSIC.load().let { it.copy(rules = it.rules.copy(startingMoney = 300, salary = 0)) }

    /** El anfitrión de la prueba: juega sus asientos con [choose] y atiende al invitado por [socket]. */
    private fun hostSide(host: Host, socket: Socket, seed: Long) {
        val mine = host.state.players.indices.toSet() - host.guestSeats
        val random = Random(seed)
        val reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
        val writer = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
        socket.soTimeout = 5000
        var greeted = false
        while (host.state.phase !is TurnPhase.Over) {
            while (greeted) {
                val action = choose(host.config, host.state, mine, random) ?: break
                writer.write(encode(host.play(action)) + "\n")
                if (host.last > 5_000) return
            }
            writer.flush()
            if (host.state.phase is TurnPhase.Over) break
            val message = decode(reader.readLine() ?: return)
            if (message is Message.Hello) greeted = true
            for (reply in host.receive(message)) writer.write(encode(reply) + "\n")
            writer.flush()
        }
    }

    private fun play(players: Int, guestSeats: Set<Int>, seed: Long): Pair<Host, Guest> {
        val names = List(players) { "J$it" }
        val host = Host(short, Engine.newGame(short, names, seed).state, guestSeats)
        val guest = Guest("PC")
        ServerSocket(0).use { server ->
            val side = thread { server.accept().use { hostSide(host, it, seed + 1) } }
            val result = Socket("127.0.0.1", server.localPort).use { socket ->
                socket.soTimeout = 2000
                guestSession(guest, socket.getInputStream().bufferedReader(Charsets.UTF_8), socket.getOutputStream().bufferedWriter(Charsets.UTF_8), seed + 2)
            }
            side.join(10_000)
            assertTrue(result.over, "semilla $seed: sin terminar tras ${host.last} acciones (${result.closed})")
            assertEquals(0, result.mismatches, "semilla $seed")
            assertTrue(result.proposals > 0, "semilla $seed: el PC no jugó")
            println("$players jugadores, semilla $seed: ${host.last} acciones (${result.proposals} del PC), ${host.state.turn} turnos")
        }
        return host to guest
    }

    @Test
    fun `F5_4 el PC juega solo por TCP hasta el final y su partida es la del anfitrion`() {
        for (seed in 1L..10L) {
            val (host, guest) = play(2, setOf(1), seed)
            assertEquals(host.last, guest.last, "semilla $seed")
            assertEquals(digest(host.state), digest(guest.state!!), "semilla $seed")
        }
    }

    @Test
    fun `F5_4 con dos jugadores en el PC y dos en el telefono`() {
        for (seed in 1L..5L) {
            val (host, guest) = play(4, setOf(1, 3), seed)
            assertEquals(digest(host.state), digest(guest.state!!), "semilla $seed")
        }
    }

    @Test
    fun `F5_4 choose no juega por los asientos del otro lado`() {
        val state = Engine.newGame(short, listOf("Ana", "PC"), 7).state
        assertNull(choose(short, state, setOf(1 - state.current), Random(1)))
        assertEquals(Action.Roll, choose(short, state, setOf(state.current), Random(1)))
    }

    @Test
    fun `F5_4 lee las salas de avahi con nombres escapados`() {
        val avahi = """
            +;wlo1;IPv4;Redmi\032Note\03213\032Pro;_mono._tcp;local
            =;wlo1;IPv4;Redmi\032Note\03213\032Pro;_mono._tcp;local;Redmi.local;192.168.1.5;40123;
            =;wlo1;IPv4;Tel\195\169fono\032de\032Ana;_mono._tcp;local;ana.local;192.168.1.7;40000;
            =;wlo1;IPv6;Redmi\032Note\03213\032Pro;_mono._tcp;local;Redmi.local;fe80::1;40123;
        """.trimIndent()
        assertEquals(listOf(Triple("Redmi Note 13 Pro", "192.168.1.5", 40123), Triple("Teléfono de Ana", "192.168.1.7", 40000)), rooms(avahi))
    }
}
