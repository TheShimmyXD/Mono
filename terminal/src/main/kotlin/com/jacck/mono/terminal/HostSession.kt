package com.jacck.mono.terminal

import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.link.actor
import com.jacck.mono.engine.link.decode
import com.jacck.mono.engine.link.encode
import com.jacck.mono.engine.model.TurnPhase
import java.io.IOException
import java.io.Writer
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread
import kotlin.random.Random

/** Cómo terminó la sala del PC: si la partida acabó, cuántas conexiones atendió y por qué paró si no. */
data class RoomResult(val over: Boolean, val connections: Int, val stopped: String? = null)

/**
 * El PC como sala (F5.10, D-76), para probar el teléfono invitado: atiende en [listening] al invitado
 * de [host] y juega los demás asientos con [choose], uno cada [pauseMs], desde que llega su saludo.
 * Como la sala del teléfono (`acceptLoop` y `hostLoop` de la app, F5.5, D-74): una conexión nueva
 * reemplaza a la anterior y la cierra, y [silenceMs] sin leer nada corta la conexión. Lo que se manda
 * sale bajo el candado de [host], en el orden en que se aplicó. [say] recibe una línea por acción
 * y por conexión. Acaba con la partida, o si nadie está conectado durante [giveUpMs].
 */
fun hostRoom(
    host: Host,
    listening: ServerSocket,
    seed: Long,
    say: (String) -> Unit = {},
    pauseMs: Long = 1_000,
    silenceMs: Int = 30_000,
    giveUpMs: Long = 300_000,
): RoomResult {
    val mine = host.state.players.indices.toSet() - host.guestSeats
    val random = Random(seed)
    var writer: Writer? = null // la conexión vigente (bajo el candado de host)
    var current: Socket? = null
    var greeted = false
    var connections = 0
    var aloneSince = System.currentTimeMillis()
    fun send(messages: List<Message>, to: Writer?) {
        if (to == null || messages.isEmpty()) return
        try {
            for (m in messages) to.write(encode(m) + "\n")
            to.flush()
        } catch (_: IOException) {} // la conexión cayó: el invitado lo pedirá al volver
    }
    fun line(m: Message.Applied, state: com.jacck.mono.engine.model.GameState) =
        say("#${m.n} ${state.players[actor(state, m.action)].name}: ${m.action}")
    thread(name = "sala-pc", isDaemon = true) {
        while (!listening.isClosed) {
            val s = try { listening.accept() } catch (_: IOException) { break }
            s.tcpNoDelay = true
            s.soTimeout = silenceMs
            val out = s.getOutputStream().bufferedWriter(Charsets.UTF_8)
            val old = synchronized(host) {
                connections++
                current.also { current = s; writer = out }
            }
            old?.close()
            say("Conexión ${connections} desde ${s.inetAddress.hostAddress}" + if (old != null) " (reemplaza a la anterior)" else "")
            thread(name = "sesion-pc", isDaemon = true) {
                val reason = try {
                    val reader = s.getInputStream().bufferedReader(Charsets.UTF_8)
                    while (true) {
                        val text = reader.readLine() ?: break
                        val message = try { decode(text) } catch (_: IllegalArgumentException) { continue }
                        if (message is Message.Bye) break
                        synchronized(host) {
                            if (message is Message.Hello) { greeted = true; say("Hola de ${message.name}, protocolo ${message.version}.") }
                            val before = host.state
                            val replies = host.receive(message)
                            replies.filterIsInstance<Message.Applied>().filter { message is Message.Propose }.forEach { line(it, before) }
                            send(replies, out)
                        }
                    }
                    "el invitado cerró la conexión"
                } catch (_: SocketTimeoutException) {
                    "${silenceMs / 1000} s sin noticias del invitado"
                } catch (e: IOException) {
                    e.message ?: "conexión rota"
                } finally {
                    s.close()
                }
                synchronized(host) {
                    if (current === s) {
                        current = null; writer = null
                        aloneSince = System.currentTimeMillis()
                        say("Se cortó: $reason (acción ${host.last}).")
                    }
                }
            }
        }
    }
    try {
        while (host.state.phase !is TurnPhase.Over) {
            val played = synchronized(host) {
                if (current == null && System.currentTimeMillis() - aloneSince > giveUpMs) return RoomResult(false, connections, "nadie conectado en ${giveUpMs / 1000} s")
                if (!greeted) return@synchronized false
                val before = host.state
                val action = choose(host.config, before, mine, random) ?: return@synchronized false
                val applied = host.play(action)
                line(applied, before)
                send(listOf(applied), writer)
                true
            }
            Thread.sleep(if (played) pauseMs else 50)
        }
        // Que el último `Applied` (el de una jugada del invitado) termine de salir antes de cerrar.
        Thread.sleep(500)
        return RoomResult(true, connections)
    } finally {
        listening.close()
        synchronized(host) { current?.close() }
    }
}
