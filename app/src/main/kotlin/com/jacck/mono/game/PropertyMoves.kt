package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase

/** Una jugada sobre una propiedad que el motor acepta ahora: lo que cuesta o da y si es el hotel. */
data class Move(val action: Action, val amount: Int, val hotel: Boolean = false)

/**
 * Las jugadas que el motor acepta sobre `square` en este momento (F3.4, D-24): construir una casa
 * o el hotel, vender un edificio, hipotecar o levantar. La interfaz no decide reglas: prueba cada
 * acción con `Engine.tryApply` y toma la cifra de su evento.
 */
fun propertyMoves(config: GameConfig, state: GameState, square: Int): List<Move> =
    listOf(Action.Build(square), Action.SellBuilding(square), Action.Mortgage(square), Action.Unmortgage(square))
        .mapNotNull { action ->
            val events = Engine.tryApply(config, state, action)?.events ?: return@mapNotNull null
            Move(action, events.sumOf { it.amountOn(square) }, hotel = events.any { it is Event.HotelBuilt })
        }

private fun Event.amountOn(square: Int): Int = when {
    this is Event.HouseBuilt && this.square == square -> price
    this is Event.HotelBuilt && this.square == square -> price
    this is Event.BuildingSold && this.square == square -> amount
    this is Event.Mortgaged && this.square == square -> amount
    this is Event.Unmortgaged && this.square == square -> amount
    else -> 0
}

/**
 * Si la hoja de [player] lleva jugadas (FC.3, D-60): solo la de quien juega, en su turno en este
 * teléfono ([localTurn]: ni del otro teléfono ni de la máquina) y antes de tirar o al terminar.
 * La de los demás solo se mira.
 */
fun canManage(state: GameState, player: Int, localTurn: Boolean): Boolean =
    localTurn && player == state.current && (state.phase == TurnPhase.Roll || state.phase == TurnPhase.EndOfTurn)
