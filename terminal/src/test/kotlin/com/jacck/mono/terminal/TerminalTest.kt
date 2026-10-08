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
import java.io.IOException
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

    /**
     * Una partida por TCP con cortes (F5.5): la sala de la prueba atiende una conexión tras otra; cuando
     * llega a cada número de [cuts] deja la conexión muda ([silent], como una red caída: ni la cierra) o la
     * cierra, y mientras tanto juega sus turnos (esas jugadas se pierden). La sala atiende hasta que el PC
     * se va, aunque la partida acabe durante un corte. Devuelve las reconexiones y los cortes que hubo.
     */
    private fun playWithCuts(seed: Long, cuts: List<Int>, silent: Boolean): Triple<GuestResult, Int, Int> {
        val host = Host(short, Engine.newGame(short, listOf("J0", "J1"), seed).state, setOf(1))
        val guest = Guest("PC")
        val random = Random(seed + 1)
        val pending = cuts.toMutableList()
        val dead = mutableListOf<Socket>()
        var greeted = false
        fun cutNow() = pending.isNotEmpty() && host.last >= pending.first()
        ServerSocket(0).use { server ->
            val side = thread {
                while (host.last < 5_000) {
                    val socket = try { server.accept() } catch (_: IOException) { break }
                    socket.soTimeout = 5000
                    val reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
                    val writer = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
                    try {
                        while (!cutNow()) {
                            while (greeted && !cutNow()) writer.write(encode(host.play(choose(host.config, host.state, setOf(0), random) ?: break)) + "\n")
                            writer.flush()
                            if (cutNow()) break
                            val message = decode(reader.readLine() ?: break)
                            if (message is Message.Hello) greeted = true
                            for (reply in host.receive(message)) writer.write(encode(reply) + "\n")
                            writer.flush()
                        }
                    } catch (_: IOException) {}
                    if (!cutNow()) { socket.close(); if (host.state.phase is TurnPhase.Over) break else continue }
                    pending.removeAt(0)
                    if (silent) dead += socket else socket.close()
                    // Corte: el anfitrión sigue con sus turnos; el invitado no se entera hasta volver.
                    while (true) host.play(choose(host.config, host.state, setOf(0), random) ?: break)
                }
            }
            val mine = mutableListOf<Socket>()
            val (result, returns) = guestLink(
                guest, seed + 2,
                connect = {
                    try {
                        Socket("127.0.0.1", server.localPort).also { mine += it; it.soTimeout = 300 }
                            .let { it.getInputStream().bufferedReader(Charsets.UTF_8) to it.getOutputStream().bufferedWriter(Charsets.UTF_8) }
                    } catch (_: IOException) { null }
                },
                beatMs = 150, retryMs = 100, giveUpMs = 10_000,
            )
            mine.forEach(Socket::close)
            side.join(10_000)
            dead.forEach(Socket::close)
            assertTrue(result.over, "semilla $seed: sin terminar tras ${host.last} acciones (${result.lost ?: result.closed})")
            assertEquals(0, result.mismatches, "semilla $seed")
            assertEquals(host.last, guest.last, "semilla $seed")
            assertEquals(digest(host.state), digest(guest.state!!), "semilla $seed")
            val done = cuts.size - pending.size
            println("cortes ${if (silent) "mudos" else "cerrados"}, semilla $seed: ${host.last} acciones, ${result.proposals} del PC, $done cortes, $returns reconexiones")
            return Triple(result, returns, done)
        }
    }

    @Test
    fun `F5_5 la red del PC se cae sin aviso y la partida sigue igual al volver`() {
        for (seed in 1L..5L) playWithCuts(seed, listOf(30, 90), silent = true).let { (_, returns, cuts) -> assertTrue(cuts > 0); assertEquals(cuts, returns, "semilla $seed") }
    }

    @Test
    fun `F5_5 la sala cierra la conexion y el PC vuelve y termina la partida`() {
        for (seed in 6L..10L) playWithCuts(seed, listOf(30, 90), silent = false).let { (_, returns, cuts) -> assertTrue(cuts > 0); assertEquals(cuts, returns, "semilla $seed") }
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
