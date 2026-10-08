package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

/** La máquina en la app (F5.8b, D-55): juega solo los asientos que le tocan. */
class MachineTest {

    /** La partida corta de F5.4 (D-50): el Clásico con $300 al empezar y sin salario. */
    private val short = Preset.CLASSIC.load().let { it.copy(rules = it.rules.copy(startingMoney = 300, salary = 0)) }

    @Test
    fun `F5-8b con todos los jugadores de maquina, las partidas cortas terminan`() {
        (1L..5L).forEach { seed ->
            var state = Engine.newGame(short, listOf("Ana", "Beto", "Caro"), seed).state
            val random = Random(seed)
            var actions = 0
            while (state.phase !is TurnPhase.Over && actions < 20_000) {
                val action = Machine.next(short, state, setOf(0, 1, 2), random)
                    ?: error("semilla $seed: la máquina no tiene jugada en ${state.phase}")
                state = Engine.apply(short, state, action).state
                actions++
            }
            assertTrue(state.phase is TurnPhase.Over, "semilla $seed: sin terminar tras $actions jugadas")
        }
    }

    @Test
    fun `F5-8b en el turno de una persona la maquina no juega`() {
        val state = Engine.newGame(short, listOf("Ana", "Beto"), 7).state
        val person = state.current
        assertNull(Machine.next(short, state, setOf(1 - person), Random(1)))
        assertTrue(Machine.next(short, state, setOf(person), Random(1)) != null)
    }
}
