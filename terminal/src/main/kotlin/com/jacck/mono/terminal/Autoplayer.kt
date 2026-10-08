package com.jacck.mono.terminal

import com.jacck.mono.engine.bot.SimpleBot
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase
import kotlin.random.Random

/**
 * Jugador automático del PC (F5.4, D-50): la acción que toca a uno de [seats] en [state], o null si
 * ahora no le toca a ninguno. Juega la máquina simple del motor (`SimpleBot`, D-54); en una subasta
 * pregunta en el orden de la subasta. El azar es suyo ([random], con semilla), no el de la partida (D-03).
 */
fun choose(config: GameConfig, state: GameState, seats: Set<Int>, random: Random): Action? {
    val order = (state.phase as? TurnPhase.Auction)?.bidders ?: seats.sorted()
    return order.filter { it in seats }.firstNotNullOfOrNull { SimpleBot.choose(config, state, it, random) }
}
