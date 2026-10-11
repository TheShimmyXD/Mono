package com.jacck.mono.engine.model

import kotlinx.serialization.Serializable

/**
 * Una partida del historial (FH.2, D-92): la app la guarda en `historial.json` y el motor no la usa;
 * vive aquí, junto a [SavedGame], porque aquí está el JSON.
 */
@Serializable
data class GameRecord(
    /** Cuándo empezó, en ms desde 1970; también la identifica. */
    val started: Long,
    val board: String,
    val players: List<RecordPlayer>,
    val turns: Int = 0,
    /** Tiempo jugado, sumado entre las veces que se siguió desde el menú. */
    val playedMs: Long = 0,
    /** Índices de quienes ganaron; null mientras no termina (o si se dejó). */
    val winners: List<Int>? = null,
    /** Cómo terminó; null mientras no termina. */
    val end: EndCondition? = null,
    /** La de `partida.json`: «Seguir la partida» la continúa. */
    val open: Boolean = false,
)

/** Un jugador del historial: [token] es su personaje; [machine] la juega la máquina, [remote] está en otro teléfono. */
@Serializable
data class RecordPlayer(val name: String, val token: String? = null, val machine: Boolean = false, val remote: Boolean = false)
