package com.jacck.mono.terminal

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase
import kotlin.random.Random

/**
 * Jugador automático del PC (F5.4, D-50): la acción que toca a uno de [seats] en [state], o null si
 * ahora no le toca a ninguno. Compra siempre que puede y a veces construye, para que la partida
 * termine; en la subasta pasa. El azar es suyo ([random], con semilla), no el de la partida (D-03).
 * Prueba cada candidata con el motor y devuelve la primera que vale.
 */
fun choose(config: GameConfig, state: GameState, seats: Set<Int>, random: Random): Action? {
    fun owned(player: Int) = state.holdings.filterValues { it.owner == player }.keys.toList()
    val options = when (val phase = state.phase) {
        is TurnPhase.Over -> return null
        is TurnPhase.Auction -> phase.bidders.filter { it in seats }.map { Action.PassBid(it) }
        is TurnPhase.Debt -> {
            val debtor = phase.debts.first().debtor
            if (debtor !in seats) return null
            owned(debtor).map { Action.SellBuilding(it) } + owned(debtor).map { Action.Mortgage(it) } + Action.DeclareBankruptcy
        }
        else -> {
            val me = state.current
            if (me !in seats) return null
            when (phase) {
                TurnPhase.Roll -> listOf(Action.UseJailCard, Action.Roll)
                is TurnPhase.Buy -> listOf(Action.Buy, Action.Decline)
                is TurnPhase.TaxChoice -> listOf(Action.PayTax(percent = false), Action.PayTax(percent = true))
                TurnPhase.EndOfTurn -> (if (random.nextInt(3) == 0) owned(me).shuffled(random).map { Action.Build(it) } else emptyList()) + Action.EndTurn
                else -> emptyList()
            }
        }
    }
    return options.firstOrNull { Engine.tryApply(config, state, it) != null }
}
