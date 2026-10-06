package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.MonoJson
import com.jacck.mono.engine.model.StartTieRule
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Tirada inicial, dados, movimiento, sueldo y turnos (F2.2), con dados fijados. */
class MovementTest {

    private val names = listOf("Ana", "Beto", "Caro")

    /** Partida en el anillo con quien juega en `position` y le toca tirar. */
    private fun at(config: GameConfig, position: Int, current: Int = 0): GameState {
        val state = Engine.newGame(config, names, seed = 1L).state
        val players = state.players.toMutableList()
        players[current] = players[current].copy(position = position)
        return state.copy(players = players, current = current)
    }

    private fun dice(vararg pairs: Pair<Int, Int>) = pairs.map { Dice(it.first, it.second) }.iterator()

    @Test
    fun `R-06 empieza el del total mayor`() {
        val (first, events) = Engine.firstPlayer(3, StartTieRule.REROLL_TIED, dice(2 to 3, 6 to 5, 1 to 1))
        assertEquals(1, first)
        assertEquals(2, events.size)
    }

    @Test
    fun `R-06 con empate repiten solo los empatados (D-08)`() {
        // Ana 11, Beto 11, Caro 4 → repiten Ana (3) y Beto (8): empieza Beto.
        val (first, events) = Engine.firstPlayer(3, StartTieRule.REROLL_TIED, dice(5 to 6, 6 to 5, 2 to 2, 1 to 2, 4 to 4))
        assertEquals(1, first)
        assertEquals(setOf(0, 1), (events[1] as Event.StartRolled).rolls.keys)
    }

    @Test
    fun `R-43 con empate y REROLL_ALL repiten todos`() {
        // Ana 11, Beto 11, Caro 4 → repiten los tres: Ana 3, Beto 8, Caro 12 → empieza Caro.
        val (first, events) = Engine.firstPlayer(3, StartTieRule.REROLL_ALL, dice(5 to 6, 6 to 5, 2 to 2, 1 to 2, 4 to 4, 6 to 6))
        assertEquals(2, first)
        assertEquals(setOf(0, 1, 2), (events[1] as Event.StartRolled).rolls.keys)
    }

    @Test
    fun `R-07 todos empiezan en la salida con el dinero inicial (R-03, R-42)`() {
        val config = ringBoard(start = 5, rules = testRules(startingMoney = 26400))
        val state = Engine.newGame(config, names, seed = 1L).state
        assertTrue(state.players.all { it.position == 5 && it.money == 26400 })
        assertEquals(TurnPhase.Roll, state.phase)
    }

    @Test
    fun `R-07 avanza la suma de los dados y varias fichas comparten casilla`() {
        val config = ringBoard()
        val afterAna = Engine.roll(config, at(config, 0, current = 0), Dice(3, 4)).state
        assertEquals(7, afterAna.players[0].position)
        assertEquals(TurnPhase.EndOfTurn, afterAna.phase)
        val beto = afterAna.copy(current = 1, phase = TurnPhase.Roll)
        val afterBeto = Engine.roll(config, beto, Dice(5, 2)).state
        assertEquals(7, afterBeto.players[0].position)
        assertEquals(7, afterBeto.players[1].position)
    }

    @Test
    fun `R-06 el turno pasa al siguiente de la lista y salta a los quebrados`() {
        val config = ringBoard()
        val end = at(config, 0, current = 2).copy(phase = TurnPhase.EndOfTurn, turn = 4)
        val next = Engine.apply(config, end, Action.EndTurn).state
        assertEquals(0, next.current)
        assertEquals(5, next.turn)
        val players = end.players.toMutableList().also { it[0] = it[0].copy(bankrupt = true) }
        assertEquals(1, Engine.apply(config, end.copy(players = players), Action.EndTurn).state.current)
    }

    @Test
    fun `R-09 con dobles vuelve a tirar y sin dobles termina`() {
        val config = ringBoard()
        val first = Engine.roll(config, at(config, 0), Dice(3, 3))
        assertEquals(TurnPhase.Roll, first.state.phase)
        assertEquals(0, first.state.current)
        assertEquals(1, first.state.doublesInRow)
        assertTrue(Event.RollAgain(0) in first.events)
        val second = Engine.roll(config, first.state, Dice(2, 5)).state
        assertEquals(13, second.players[0].position)
        assertEquals(TurnPhase.EndOfTurn, second.phase)
        assertEquals(0, second.doublesInRow)
    }

    @Test
    fun `R-43 par da otra tirada sin tope (D-08)`() {
        val config = ringBoard(rules = testRules(doublesToJail = 0))
        var state = at(config, 0)
        repeat(4) { state = Engine.roll(config, state, Dice(1, 1)).state }
        assertEquals(TurnPhase.Roll, state.phase)
        assertEquals(4, state.doublesInRow)
        assertEquals(8, state.players[0].position)
    }

    @Test
    fun `R-09 sin la opcion doublesRollAgain los dobles no dan otra tirada`() {
        val config = ringBoard(rules = testRules(doublesRollAgain = false))
        assertEquals(TurnPhase.EndOfTurn, Engine.roll(config, at(config, 0), Dice(4, 4)).state.phase)
    }

    @Test
    fun `R-10 cobra 200 al pasar por la salida`() {
        val config = ringBoard()
        val result = Engine.roll(config, at(config, 14), Dice(1, 3))
        assertEquals(2, result.state.players[0].position)
        assertEquals(1700, result.state.players[0].money)
        assertEquals(listOf(Event.SalaryPaid(0, 200)), result.events.filterIsInstance<Event.SalaryPaid>())
    }

    @Test
    fun `R-10 cobra al caer justo en la salida y no al salir de ella`() {
        val config = ringBoard()
        val landing = Engine.roll(config, at(config, 12), Dice(1, 3)).state
        assertEquals(0, landing.players[0].position)
        assertEquals(1700, landing.players[0].money)
        val leaving = Engine.roll(config, at(config, 0), Dice(2, 3)).state
        assertEquals(1500, leaving.players[0].money)
    }

    @Test
    fun `R-10 la salida puede estar en cualquier casilla del anillo`() {
        val config = ringBoard(start = 5)
        val result = Engine.roll(config, at(config, 3), Dice(1, 3)).state
        assertEquals(7, result.players[0].position)
        assertEquals(1700, result.players[0].money)
    }

    @Test
    fun `R-45 Tio Rico cobra 2000 al pasar por la Estacion Santa Fe`() {
        val config = ringBoard(rules = testRules(startingMoney = 26400, salary = 2000))
        val result = Engine.roll(config, at(config, 15), Dice(2, 1)).state
        assertEquals(2, result.players[0].position)
        assertEquals(28400, result.players[0].money)
    }

    @Test
    fun `no se tira fuera de turno ni se termina antes de tirar`() {
        val config = ringBoard()
        val state = at(config, 0)
        assertThrows<IllegalActionException> { Engine.apply(config, state, Action.EndTurn) }
        val done = state.copy(phase = TurnPhase.EndOfTurn)
        assertThrows<IllegalActionException> { Engine.apply(config, done, Action.Roll) }
    }

    /** Juega `actions` acciones (tirar o terminar, lo que toque) y devuelve cada estado. */
    private fun play(config: GameConfig, start: GameState, actions: Int): List<GameState> {
        var state = start
        return List(actions) {
            val action = if (state.phase == TurnPhase.Roll) Action.Roll else Action.EndTurn
            state = Engine.apply(config, state, action).state
            state
        }
    }

    @Test
    fun `D-03 misma semilla, misma partida`() {
        val config = ringBoard(size = 40)
        fun game(seed: Long) = play(config, Engine.newGame(config, names, seed).state, 300)
        assertEquals(game(2026L), game(2026L))
        assertNotEquals(game(2026L).last(), game(2027L).last())
    }

    @Test
    fun `D-03 una partida guardada en JSON sigue igual`() {
        val config = ringBoard(size = 40)
        val middle = play(config, Engine.newGame(config, names, 99L).state, 100).last()
        val loaded = MonoJson.decodeState(MonoJson.encodeState(middle))
        assertEquals(play(config, middle, 100), play(config, loaded, 100))
    }
}
