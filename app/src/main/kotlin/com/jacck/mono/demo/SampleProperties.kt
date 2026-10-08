package com.jacck.mono.demo

import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding

/**
 * Reparte a quien empieza, en el Clásico, los marrones (1 y 3) con 2 casas cada uno, los celestes
 * (6, 8 y 9) sin casas y la Estación de la Sabana (5) hipotecada, para probar su hoja (F3.4) en el
 * teléfono; al siguiente, 11, 13 y 15, para ver la tarjeta de otro (FC.3). Las 4 casas salen del
 * Banco, como si las hubiera construido.
 */
fun withSampleProperties(state: GameState): GameState {
    val owner = state.current
    val sample = mapOf(
        1 to Holding(owner, houses = 2), 3 to Holding(owner, houses = 2),
        6 to Holding(owner), 8 to Holding(owner), 9 to Holding(owner),
        5 to Holding(owner, mortgaged = true),
    )
    val next = (owner + 1) % state.players.size
    val other = mapOf(11 to Holding(next), 13 to Holding(next), 15 to Holding(next))
    return state.copy(holdings = state.holdings + sample + other, bankHouses = state.bankHouses - 4)
}
