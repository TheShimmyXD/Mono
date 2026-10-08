package com.jacck.mono.engine.link

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.candidates
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.ringBoard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

/** Transporte falso: cada mensaje pasa por su línea de JSON y puede perderse, duplicarse o adelantarse. */
private class FakeLink(private val random: Random, val loss: Double, val dup: Double, val shuffle: Double) {
    private val queue = ArrayList<String>()
    var sent = 0
    var lost = 0

    fun send(message: Message) {
        sent++
        val line = encode(message)
        check('\n' !in line) { "el mensaje ocupa más de una línea" }
        if (random.nextDouble() < loss) { lost++; return }
        queue += line
        if (random.nextDouble() < dup) queue += line
    }

    fun poll(): Message? {
        if (queue.isEmpty()) return null
        val i = if (queue.size > 1 && random.nextDouble() < shuffle) random.nextInt(minOf(queue.size, 4)) else 0
        return decode(queue.removeAt(i))
    }

    val empty get() = queue.isEmpty()
}

/** Elige acciones como el simulador de F2.9, solo para los jugadores de `seats`. */
private class Chooser(seed: Long) {
    private val random = Random(seed)
    private var extras = 0
    private var turn = -1

    fun pick(config: GameConfig, state: GameState, seats: Set<Int>): Action? {
        if (state.phase is TurnPhase.Over) return null
        if (state.turn != turn) { extras = 0; turn = state.turn }
        val action = candidates(config, state, random, extras)
            .firstOrNull { actor(state, it) in seats && Engine.tryApply(config, state, it) != null } ?: return null
        if (state.phase == TurnPhase.EndOfTurn && action != Action.EndTurn) extras++
        return action
    }
}

private data class LinkRun(val over: Boolean, val actions: Int, val sent: Int, val lost: Int, val resyncs: Int)

/**
 * Una partida entre anfitrión e invitado (los jugadores impares) por el transporte falso, hasta
 * que termina o llega a `maxTurns` y el invitado se pone al día. Comprueba que acaban iguales.
 */
private fun linkGame(config: GameConfig, players: Int, seed: Long, maxTurns: Int, loss: Double = 0.05, dup: Double = 0.03, shuffle: Double = 0.1): LinkRun {
    val start = Engine.newGame(config, List(players) { "J$it" }, seed).state
    val guestSeats = (0 until players).filter { it % 2 == 1 }.toSet()
    val hostSeats = (0 until players).toSet() - guestSeats
    val host = Host(config, start, guestSeats)
    val guest = Guest("PC")
    val toHost = FakeLink(Random(seed * 4 + 1), loss, dup, shuffle)
    val toGuest = FakeLink(Random(seed * 4 + 2), loss, dup, shuffle)
    val hostMind = Chooser(seed * 4 + 3)
    val guestMind = Chooser(seed * 4 + 4)
    toHost.send(guest.hello())
    var idle = 0
    var steps = 0
    var resyncs = 0
    while (true) {
        check(++steps < maxTurns * 2000) { "semilla $seed: atascada en ${host.state.phase}, anfitrión ${host.last}, invitado ${guest.last}" }
        var moved = false
        toHost.poll()?.let { m -> host.receive(m).forEach(toGuest::send); moved = true }
        toGuest.poll()?.let { m -> guest.receive(m).forEach { resyncs++; toHost.send(it) }; moved = true }
        val playing = host.state.phase !is TurnPhase.Over && host.state.turn < maxTurns
        if (playing) hostMind.pick(config, host.state, hostSeats)?.let { toGuest.send(host.play(it)); moved = true }
        val mine = guest.state
        if (playing && mine != null) guestMind.pick(config, mine, guestSeats)?.let { a -> guest.propose(a)?.let { toHost.send(it); moved = true } }
        if (!playing && guest.last == host.last && toHost.empty && toGuest.empty) break
        // Un rato sin mensajes: el invitado pide lo que le falta (el temporizador de la app).
        if (moved) idle = 0 else if (++idle >= 3) { resyncs++; guest.timeout().forEach(toHost::send); idle = 0 }
    }
    assertEquals(0, guest.mismatches, "semilla $seed: resúmenes distintos")
    assertEquals(host.state, guest.state, "semilla $seed")
    assertEquals(digest(host.state), digest(guest.state!!))
    return LinkRun(host.state.phase is TurnPhase.Over, host.last, toHost.sent + toGuest.sent, toHost.lost + toGuest.lost, resyncs)
}

class LinkTest {

    private val classic = Preset.CLASSIC.load()

    @Test
    fun `F5-2 cada mensaje va y vuelve por una linea de JSON`() {
        val state = Engine.newGame(classic, listOf("Ana", "Beto"), 7).state
        val all = listOf(
            Message.Hello(PROTOCOL_VERSION, "PC"),
            Message.Snapshot(PROTOCOL_VERSION, classic, state, setOf(1), 0),
            Message.Propose(Action.Bid(1, 120), 4),
            Message.Applied(5, Action.Roll, digest(state)),
            Message.Rejected(4, "no le toca al invitado"),
            Message.Resync(3, full = true),
            Message.Alive(9),
            Message.Bye("adiós"),
        )
        for (m in all) {
            val line = encode(m)
            assertFalse('\n' in line, line.take(60))
            assertEquals(m, decode(line))
        }
    }

    @Test
    fun `F5-2 otra version del protocolo se despide`() {
        val host = Host(classic, Engine.newGame(classic, listOf("Ana", "Beto"), 7).state, setOf(1))
        assertTrue(host.receive(Message.Hello(PROTOCOL_VERSION + 1, "PC")).single() is Message.Bye)
    }

    @Test
    fun `F5-4 el anfitrion guarda los eventos de la ultima accion, suya o del invitado`() {
        val state = Engine.newGame(classic, listOf("Ana", "Beto"), 7).state
        val host = Host(classic, state, setOf(1 - state.current))
        val expected = Engine.apply(classic, state, Action.Roll)
        host.play(Action.Roll)
        assertEquals(expected.events, host.events)
        assertTrue(host.events.isNotEmpty())
        // Hasta que le toque al invitado, y entonces su jugada también deja sus eventos.
        val random = Random(5)
        fun legal(s: GameState) = candidates(classic, s, random, 0).first { Engine.tryApply(classic, s, it) != null }
        while (actor(host.state, Action.Roll) !in host.guestSeats) host.play(legal(host.state))
        val before = host.state
        val action = legal(before)
        val reply = host.receive(Message.Propose(action, host.last)).single()
        assertTrue(reply is Message.Applied, "$reply")
        assertEquals(Engine.apply(classic, before, action).events, host.events)
    }

    @Test
    fun `F5-10 el invitado avisa cada accion aplicada con los mismos eventos que el anfitrion`() {
        val state = Engine.newGame(classic, listOf("Ana", "Beto"), 7).state
        val host = Host(classic, state, setOf(1))
        val heard = mutableListOf<Pair<Int, com.jacck.mono.engine.Result>>()
        val guest = Guest("Redmi") { n, result -> heard += n to result }
        guest.receive(host.snapshot())
        assertEquals(listOf(0), heard.map { it.first })
        assertTrue(heard.single().second.events.isEmpty())
        val random = Random(3)
        val expected = mutableListOf<List<com.jacck.mono.engine.Event>>()
        repeat(40) {
            val action = candidates(classic, host.state, random, 0).first { Engine.tryApply(classic, host.state, it) != null }
            val applied = if (actor(host.state, action) in host.guestSeats) {
                host.receive(guest.propose(action)!!).single() as Message.Applied
            } else host.play(action)
            expected += host.events
            guest.receive(applied)
        }
        assertEquals((0..40).toList(), heard.map { it.first })
        assertEquals(expected, heard.drop(1).map { it.second.events })
        assertEquals(host.state, heard.last().second.state)
        assertTrue(expected.count { it.isNotEmpty() } >= 30, "${expected.count { it.isNotEmpty() }}")
    }

    @Test
    fun `F5-2 el invitado solo propone en el turno de los suyos y el anfitrion lo comprueba`() {
        val state = Engine.newGame(classic, listOf("Ana", "Beto"), 7).state
        val guestSeat = 1 - state.current // el invitado tiene al que no empieza
        val host = Host(classic, state, setOf(guestSeat))
        val guest = Guest("PC")
        guest.receive(host.receive(guest.hello()).single())
        assertNull(guest.propose(Action.Roll), "no es su turno")
        val rejected = host.receive(Message.Propose(Action.Roll, 0)).single()
        assertTrue(rejected is Message.Rejected, "$rejected")
        assertEquals(0, host.last)
    }

    @Test
    fun `F5-2 acciones repetidas y desordenadas se aplican una vez y en orden`() {
        val state = Engine.newGame(classic, listOf("Ana", "Beto"), 7).state
        val host = Host(classic, state, emptySet())
        val guest = Guest("PC")
        guest.receive(host.receive(guest.hello()).single())
        val first = host.play(Action.Roll)
        val next = listOf(Action.Buy, Action.EndTurn, Action.Roll, Action.PayTax(false), Action.Decline)
            .first { Engine.tryApply(classic, host.state, it) != null }
        val second = host.play(next)
        assertTrue(guest.receive(second).isEmpty())
        assertEquals(0, guest.last, "la segunda espera a la primera")
        guest.receive(first)
        guest.receive(first)
        assertEquals(2, guest.last)
        assertEquals(host.state, guest.state)
    }

    @Test
    fun `F5-5 tras un corte el invitado pide desde su ultima accion y acaba igual`() {
        for (seed in 1L..20L) {
            val config = Preset.CLASSIC.load()
            val host = Host(config, Engine.newGame(config, listOf("J0", "J1", "J2"), seed).state, setOf(1))
            val guest = Guest("PC")
            val hostMind = Chooser(seed * 2)
            val guestMind = Chooser(seed * 2 + 1)
            assertEquals(Message.Hello(PROTOCOL_VERSION, "PC"), guest.resume(), "sin partida, saluda")
            guest.receive(host.receive(guest.resume()).single())
            // Un rato conectados; después se corta y el anfitrión sigue con sus jugadores hasta que le toca al invitado.
            var cut = false
            var lostWhileCut = 0
            for (step in 0 until 400) {
                if (host.state.phase is TurnPhase.Over) break
                val action = hostMind.pick(config, host.state, setOf(0, 2))
                if (step >= 60 && action != null) cut = true
                if (action != null) {
                    val applied = host.play(action)
                    if (cut) lostWhileCut++ else guest.receive(applied)
                } else if (!cut) {
                    val p = guestMind.pick(config, guest.state!!, setOf(1))?.let(guest::propose) ?: continue
                    host.receive(p).forEach { guest.receive(it) }
                } else break
            }
            assertTrue(lostWhileCut > 0, "semilla $seed: el corte no se perdió nada")
            val back = guest.resume()
            assertEquals(Message.Resync(guest.last), back, "semilla $seed: pide desde su última acción, no la partida entera")
            val replies = host.receive(back)
            assertTrue(replies.all { it is Message.Applied }, "semilla $seed: le reenvía solo lo que le falta")
            assertEquals(lostWhileCut, replies.size, "semilla $seed")
            replies.forEach { assertTrue(guest.receive(it).isEmpty()) }
            assertEquals(0, guest.mismatches)
            assertEquals(host.last, guest.last)
            assertEquals(digest(host.state), digest(guest.state!!), "semilla $seed")
            // Ya al día: el anfitrión contesta igual, para que el invitado sepa que la red sigue viva.
            assertEquals(listOf(Message.Alive(host.last)), host.receive(Message.Resync(guest.last)))
        }
    }

    @Test
    fun `F5-2 mil partidas por un enlace que pierde, repite y desordena acaban identicas`() {
        val presets = listOf(classic, Preset.TIO_RICO.load())
        val sizes = (16..48 step 4).toList()
        val maxTurns = 150
        val start = System.nanoTime()
        val runs = (0 until 1000).map { g ->
            val config = ringBoard(presets[g % 2], sizes[(g / 10) % sizes.size], Random(g))
            linkGame(config, players = 2 + (g / 2) % 5, seed = g.toLong(), maxTurns = maxTurns)
        }
        val seconds = (System.nanoTime() - start) / 1e9
        println(
            "LINK 1000 partidas en %.1f s · terminadas %d · acciones %d · mensajes %d · perdidos %d · pedidos de reenvío %d"
                .format(seconds, runs.count { it.over }, runs.sumOf { it.actions }, runs.sumOf { it.sent }, runs.sumOf { it.lost }, runs.sumOf { it.resyncs }),
        )
        assertEquals(1000, runs.size)
        assertTrue(runs.sumOf { it.lost } > 0, "el transporte falso debe perder mensajes")
    }
}
