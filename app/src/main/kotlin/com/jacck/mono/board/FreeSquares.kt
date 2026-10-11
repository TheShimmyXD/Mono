package com.jacck.mono.board

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare

/** Casillas que aún tiene el Banco: las que se compran (solares, estaciones y servicios) sin dueño (FH.1, D-90). */
fun freeSquares(config: GameConfig, state: GameState): Set<Int> =
    config.squares.indices.filter { config.squares[it] is OwnableSquare && it !in state.holdings }.toSet()
