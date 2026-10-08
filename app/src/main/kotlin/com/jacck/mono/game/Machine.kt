package com.jacck.mono.game

import com.jacck.mono.engine.bot.SimpleBot
import com.jacck.mono.engine.bot.SmartBot
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase
import kotlin.random.Random

/**
 * La máquina dentro de la app (F5.8b, D-55): si quien tiene que decidir ahora (`decider`) es uno de
 * [bots], su jugada según la máquina lista (D-54); si ella no tiene una, la de la simple. Null si
 * decide una persona o la partida terminó. El `GameViewModel` la juega después de una pausa.
 */
object Machine {
    fun next(config: GameConfig, state: GameState, bots: Set<Int>, random: Random): Action? {
        if (state.phase is TurnPhase.Over) return null
        val seat = decider(state).takeIf { it in bots } ?: return null
        return SmartBot.choose(config, state, seat, random) ?: SimpleBot.choose(config, state, seat, random)
    }
}
