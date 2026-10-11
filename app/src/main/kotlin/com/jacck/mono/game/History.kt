package com.jacck.mono.game

import android.os.SystemClock
import android.util.Log
import com.jacck.mono.LOG_TAG
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameRecord
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.MonoJson
import com.jacck.mono.engine.model.RecordPlayer
import com.jacck.mono.engine.model.TurnPhase
import java.io.File
import java.time.Instant
import java.time.ZoneId

/**
 * El historial (FH.2, D-92): `historial.json` en la carpeta privada de la app, de la más vieja a la
 * más nueva. Se escribe a un temporal y luego se renombra, como `partida.json` (D-26). Si no se
 * puede leer, se aparta como `historial_danado.json` y el historial vuelve a empezar.
 */
class HistoryFile(dir: File) {

    private val file = File(dir, "historial.json")
    private val temp = File(dir, "historial.json.tmp")
    private val damaged = File(dir, "historial_danado.json")

    fun read(): List<GameRecord> {
        if (!file.exists()) return emptyList()
        return try {
            MonoJson.decodeHistory(file.readText())
        } catch (_: IllegalArgumentException) {
            file.renameTo(damaged) // SerializationException es un IllegalArgumentException
            emptyList()
        }
    }

    /** La que continúa «Seguir la partida», si hay. */
    fun open(): GameRecord? = read().lastOrNull { it.open }

    /** Una partida nueva; si es [GameRecord.open], la que estaba abierta queda «sin terminar». */
    fun add(record: GameRecord) =
        write(read().map { if (record.open) it.copy(open = false) else it } + record)

    /** Pone [record] en lugar de la que empezó a la misma hora (o la agrega, si no está). */
    fun update(record: GameRecord) {
        val all = read()
        write(if (all.any { it.started == record.started }) all.map { if (it.started == record.started) record else it } else all + record)
    }

    private fun write(records: List<GameRecord>) {
        temp.writeText(MonoJson.encodeHistory(records))
        check(temp.renameTo(file)) { "no se pudo guardar el historial en $file" }
    }
}

/** Tiempo jugado: [base] de las veces anteriores más lo que va desde [start] (ms de un reloj que no salta). */
class PlayClock(private var base: Long) {
    private var since: Long? = null

    fun start(now: Long) {
        if (since == null) since = now
    }

    fun stop(now: Long) {
        since?.let { base += now - it }
        since = null
    }

    fun total(now: Long): Long = base + (since?.let { now - it } ?: 0)
}

/** El registro de una partida que empieza: [machines] los juega la máquina, [remote] están en otro teléfono. */
fun newRecord(config: GameConfig, state: GameState, started: Long, machines: Set<Int>, remote: Set<Int>, open: Boolean) = GameRecord(
    started, config.name,
    state.players.mapIndexed { k, p -> RecordPlayer(p.name, p.token, machine = k in machines, remote = k in remote) },
    turns = state.turn, open = open,
)

/** El registro tras una jugada: turnos, tiempo y, si terminó, quiénes ganaron y cómo. */
fun GameRecord.after(config: GameConfig, state: GameState, playedMs: Long): GameRecord {
    val over = state.phase as? TurnPhase.Over
    return copy(
        turns = state.turn, playedMs = playedMs, winners = over?.winners, end = over?.let { config.rules.endCondition },
        open = open && over == null,
    )
}

/** Horas y minutos de [ms] («1 h 12 min», «48 min»). */
fun hoursMinutes(ms: Long): Pair<Long, Long> = (ms / 60_000).let { it / 60 to it % 60 }

/** Día de la semana (0 = domingo), día del mes y mes (0 = enero) de [ms] en [zone]. */
fun dayMonth(ms: Long, zone: ZoneId = ZoneId.systemDefault()): Triple<Int, Int, Int> =
    Instant.ofEpochMilli(ms).atZone(zone).let { Triple(it.dayOfWeek.value % 7, it.dayOfMonth, it.monthValue - 1) }

/**
 * Lleva el registro de una partida al historial: suma el tiempo mientras se ve (`start`, `stop`) y
 * lo escribe tras cada jugada. Una partida terminada ya no cambia.
 */
class HistoryKeeper(
    private val file: HistoryFile,
    private var record: GameRecord,
    private val now: () -> Long = SystemClock::elapsedRealtime,
    private val log: (String) -> Unit = { Log.i(LOG_TAG, it) },
) {
    private var clock = PlayClock(record.playedMs)

    /** De este teléfono (la sigue «Seguir la partida»); las del enlace no. */
    private val local = record.open

    fun start() = clock.start(now())

    fun stop(config: GameConfig, state: GameState) {
        clock.stop(now())
        save(config, state)
    }

    fun update(config: GameConfig, state: GameState) = save(config, state)

    /** «Otra partida» con los mismos jugadores: un registro nuevo, que empieza a contar ya. */
    fun restart(state: GameState, started: Long) {
        clock.stop(now())
        if (record.winners == null) file.update(record.copy(playedMs = clock.total(now()))) // la que se deja, sin terminar
        record = record.copy(started = started, turns = state.turn, playedMs = 0, winners = null, end = null, open = local)
        file.add(record)
        clock = PlayClock(0).also { it.start(now()) }
        log("historial: otra partida, ${record.started}")
    }

    private fun save(config: GameConfig, state: GameState) {
        if (record.winners != null) return
        record = record.after(config, state, clock.total(now()))
        file.update(record)
        log("historial: ${record.started} turno ${record.turns}, ${record.playedMs / 1000} s${record.winners?.let { ", ganó $it" } ?: ""}")
    }
}
