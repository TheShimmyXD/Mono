package com.jacck.mono.game

import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuctionTurnTest {

    private fun auction(bidders: List<Int>, leader: Int? = null) =
        TurnPhase.Auction(square = 6, bidders = bidders, highestBid = if (leader == null) 0 else 50, highestBidder = leader)

    @Test
    fun `sin pujas empieza el primero de la lista`() {
        assertEquals(2, nextBidder(auction(listOf(2, 3, 0, 1))))
    }

    @Test
    fun `después de una puja sigue el de la derecha`() {
        assertEquals(3, nextBidder(auction(listOf(2, 3, 0, 1), leader = 2)))
    }

    @Test
    fun `al final de la lista vuelve al principio`() {
        assertEquals(2, nextBidder(auction(listOf(2, 3, 0, 1), leader = 1)))
    }

    @Test
    fun `quien pasa sale y la ronda sigue con el siguiente`() {
        // 2 puja, 3 pasa (sale de la lista): le toca a 0.
        assertEquals(0, nextBidder(auction(listOf(2, 0, 1), leader = 2)))
    }
}
