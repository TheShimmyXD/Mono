package com.jacck.mono.terminal

import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.model.TurnPhase
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

private const val SERVICE_TYPE = "_mono._tcp" // SERVICE_TYPE de app/.../enlace/LanServer.kt
private const val USAGE = """Uso: mono-pc [--ip 192.168.1.5 --puerto 40000] [--nombre PC] [--semilla 7] [--espera 10]
Se une a la sala del teléfono (por mDNS con avahi-browse, o la dirección que muestra la sala) y juega
solo con los jugadores «📶 Otro teléfono» hasta que la partida termina (F5.4, D-50). Si se corta la red,
vuelve a conectar cada 2 s durante 5 min y sigue donde iba (F5.5); --espera = segundos de silencio por aviso."""

/** (nombre, IPv4, puerto) de cada sala resuelta en la salida de `avahi-browse -rpt _mono._tcp` (como `pc/invitado.py`). */
fun rooms(avahi: String): List<Triple<String, String, Int>> = avahi.lines().mapNotNull { line ->
    val c = line.split(";")
    if (c.size < 9 || c[0] != "=" || c[2] != "IPv4" || c[4] != SERVICE_TYPE) return@mapNotNull null
    Triple(unescape(c[3]), c[7], c[8].toInt())
}.distinct()

/** avahi escapa cada byte raro como \ddd (decimal): «Redmi\032Note» = «Redmi Note»; una «é» son dos bytes. */
private fun unescape(text: String): String {
    val bytes = ByteArrayOutputStream()
    var from = 0
    for (m in Regex("""\\(\d{3})""").findAll(text)) {
        bytes.write(text.substring(from, m.range.first).toByteArray())
        bytes.write(m.groupValues[1].toInt())
        from = m.range.last + 1
    }
    bytes.write(text.substring(from).toByteArray())
    return bytes.toString(Charsets.UTF_8)
}

fun main(args: Array<String>) {
    val opts = args.toList().chunked(2).associate { it[0] to it.getOrElse(1) { "" } }
    if ("--help" in args || "-h" in args || opts.keys.any { it !in setOf("--ip", "--puerto", "--nombre", "--semilla", "--espera") }) {
        println(USAGE)
        exitProcess(if ("--help" in args || "-h" in args) 0 else 2)
    }
    var ip = opts["--ip"]
    var port = opts["--puerto"]?.toInt()
    if (ip == null || port == null) {
        val avahi = ProcessBuilder("avahi-browse", "-rpt", SERVICE_TYPE).redirectErrorStream(true).start()
        avahi.waitFor(20, TimeUnit.SECONDS)
        val found = rooms(avahi.inputStream.bufferedReader().readText())
        if (found.isEmpty()) {
            println("No veo ninguna sala $SERVICE_TYPE en la red (¿mismo Wi-Fi y sala abierta?). Prueba con --ip y --puerto.")
            exitProcess(1)
        }
        val (name, foundIp, foundPort) = found.first()
        println("Sala «$name» en $foundIp:$foundPort" + if (found.size > 1) " (hay ${found.size})" else "")
        ip = foundIp
        port = foundPort
    }
    val seed = opts["--semilla"]?.toLong() ?: System.currentTimeMillis()
    val guest = Guest(opts["--nombre"] ?: "PC")
    val start = System.currentTimeMillis()
    val wait = (opts["--espera"]?.toInt() ?: 10) * 1000
    var socket: Socket? = null
    // Cada conexión nueva cierra la anterior; tras un corte se vuelve a la misma dirección (F5.5, D-74).
    val connect = {
        socket?.close()
        try {
            Socket().also { s ->
                s.connect(InetSocketAddress(ip, port), 15_000)
                s.tcpNoDelay = true
                s.soTimeout = wait
                socket = s
            }.let { it.getInputStream().bufferedReader(Charsets.UTF_8) to it.getOutputStream().bufferedWriter(Charsets.UTF_8) }
        } catch (e: IOException) {
            println("No pude conectar con $ip:$port (${e.message}).")
            null
        }
    }
    println("Conectando con $ip:$port; semilla del PC $seed.")
    // Detenido a mano (Ctrl+C) a mitad de partida: que diga al menos si las dos partidas iban iguales.
    var finished = false
    Runtime.getRuntime().addShutdownHook(Thread {
        if (!finished) println("Detenido en la acción ${guest.last}, turno ${guest.state?.turn ?: 0}; ${guest.mismatches} resúmenes distintos.")
    })
    val (result, returns) = guestLink(guest, seed, connect, ::println, beatMs = wait / 2L)
    finished = true
    socket?.close()
    val minutes = (System.currentTimeMillis() - start) / 60_000.0
    val state = guest.state
    val winners = (state?.phase as? TurnPhase.Over)?.winners?.joinToString(", ") { state.players[it].name }
    println(
        (if (result.over) "Partida terminada: gana $winners" else "Sin terminar (${result.closed ?: result.lost ?: "se cerró la conexión"})") +
            "; ${guest.last} acciones, ${state?.turn ?: 0} turnos, ${result.proposals} del PC, ${result.mismatches} resúmenes distintos, " +
            "$returns reconexiones, " +
            "%.1f min.".format(minutes),
    )
    exitProcess(if (result.over && result.mismatches == 0) 0 else 1)
}
