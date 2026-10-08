package com.jacck.mono.game

import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.TurnPhase

/**
 * A quién le toca pujar en un solo teléfono (D-23): el siguiente de `bidders` después de quien va
 * ganando, o el primero si nadie ha pujado. Quien pasa sale de la lista, así que la ronda sigue sola.
 * El motor acepta pujas de cualquiera (R-12); esto solo ordena quién tiene el teléfono.
 */
fun nextBidder(auction: TurnPhase.Auction): Int {
    val bidders = auction.bidders
    val leader = auction.highestBidder ?: return bidders.first()
    return bidders[(bidders.indexOf(leader) + 1) % bidders.size]
}

/**
 * Quién tiene que decidir ahora (F5.4): el siguiente postor en una subasta, el primer deudor en una
 * deuda y, si no, el del turno. En una partida enlazada, si es del otro teléfono, aquí se espera.
 */
fun decider(state: GameState): Int = when (val phase = state.phase) {
    is TurnPhase.Auction -> nextBidder(phase)
    is TurnPhase.Debt -> phase.debts.first().debtor
    else -> state.current
}
