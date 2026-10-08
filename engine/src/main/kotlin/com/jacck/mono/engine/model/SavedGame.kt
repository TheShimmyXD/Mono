package com.jacck.mono.engine.model

import kotlinx.serialization.Serializable

/**
 * Una partida guardada (F3.6, D-26): la configuración con que se juega y el estado, azar incluido
 * (D-03), así que al cargarla sigue exactamente igual. La configuración va completa y no solo el
 * nombre del preset, para que sirva también con los tableros del editor (F4).
 * `bots`: los jugadores que juega la máquina (F5.8b, D-55); una partida guardada antes los carga vacíos.
 */
@Serializable
data class SavedGame(val config: GameConfig, val state: GameState, val bots: Set<Int> = emptySet())
