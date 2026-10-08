package com.jacck.mono.engine.bot

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class BotsTest {
    /** La partida corta de F5.4 (D-50): el Clásico con $300 al empezar y sin salario. */
    private val short = Preset.CLASSIC.load().let { it.copy(rules = it.rules.copy(startingMoney = 300, salary = 0)) }

    /** Juega [bots] (uno por asiento) hasta que termine o pasen [maxActions]; en la subasta pregunta en su orden. */
    private fun play(config: GameConfig, bots: List<Bot>, seed: Long, maxActions: Int = 20_000): GameState {
        val random = Random(seed)
        var state = Engine.newGame(config, bots.indices.map { "J$it" }, seed).state
        repeat(maxActions) {
            if (state.phase is TurnPhase.Over) return state
            val order = (state.phase as? TurnPhase.Auction)?.bidders ?: bots.indices.toList()
            val action = order.firstNotNullOfOrNull { bots[it].choose(config, state, it, random) }
                ?: error("semilla $seed: nadie juega en ${state.phase}")
            state = Engine.apply(config, state, action).state
        }
        return state
    }

    @Test
    fun `F5-8 la maquina lista gana al menos 3 de 10 partidas cortas contra la simple`() {
        var wins = 0
        var over = 0
        val turns = mutableListOf<Int>()
        for (seed in 1L..10L) {
            val smart = (seed % 2).toInt() // se alternan los asientos
            val bots = if (smart == 0) listOf(SmartBot, SimpleBot) else listOf(SimpleBot, SmartBot)
            val end = play(short, bots, seed)
            val phase = end.phase
            if (phase is TurnPhase.Over) {
                over++
                if (phase.winners == listOf(smart)) wins++
            }
            turns += end.turn
        }
        println("F5.8: la lista gana $wins/10; terminadas $over/10; turnos ${turns.sorted()}")
        assertTrue(wins >= 3, "la lista ganó $wins/10")
    }

    @Test
    fun `F5-8 la maquina no juega cuando no le toca`() {
        val state = Engine.newGame(short, listOf("Ana", "PC"), 7).state
        val other = 1 - state.current
        assertNull(SmartBot.choose(short, state, other, Random(1)))
        assertNull(SimpleBot.choose(short, state, other, Random(1)))
        assertEquals(Action.Roll, SmartBot.choose(short, state, state.current, Random(1)))
    }

    @Test
    fun `F5-8 en la subasta la lista puja y la simple pasa`() {
        val start = Engine.newGame(short, listOf("Ana", "PC"), 7).state
        val square = short.squares.indices.first { short.squares[it] is Property }
        val auction = Engine.apply(short, start.copy(phase = TurnPhase.Buy(square)), Action.Decline).state
        val phase = auction.phase as TurnPhase.Auction
        val me = phase.bidders.first()
        assertTrue(SmartBot.choose(short, auction, me, Random(1)) is Action.Bid)
        assertEquals(Action.PassBid(me), SimpleBot.choose(short, auction, me, Random(1)))
    }
}
