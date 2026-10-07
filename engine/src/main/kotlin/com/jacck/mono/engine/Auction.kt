package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.TurnPhase

/**
 * Subasta de `square` (R-12, R-44): pujan todos los que siguen en juego, desde quien juega e
 * incluido quien no la quiso comprar. `queue` = casillas que se subastan después (R-35) y
 * `then` = la fase al terminar (null: sigue el turno de quien juega).
 */
internal fun startAuction(
    config: GameConfig,
    state: GameState,
    square: Int,
    events: MutableList<Event>,
    queue: List<Int> = emptyList(),
    then: TurnPhase? = null,
): GameState {
    val count = state.players.size
    val bidders = (0 until count).map { (state.current + it) % count }.filter { !state.players[it].bankrupt }
    events += Event.AuctionStarted(square, minimumBid(config, square))
    return state.copy(phase = TurnPhase.Auction(square, bidders, highestBid = 0, highestBidder = null, queue = queue, then = then))
}

/** Puja `amount`: más que la mayor, desde la base y con dinero para pagarla (R-12, R-44). */
internal fun bid(config: GameConfig, state: GameState, player: Int, amount: Int): Result {
    val auction = state.phase as? TurnPhase.Auction ?: throw IllegalActionException("no hay subasta: ${state.phase}")
    if (player !in auction.bidders) throw IllegalActionException("ya no puja: $player")
    if (amount <= auction.highestBid) throw IllegalActionException("hay que superar ${auction.highestBid}")
    if (amount < minimumBid(config, auction.square)) throw IllegalActionException("por debajo de la base (R-44)")
    if (state.players[player].money < amount) throw IllegalActionException("no alcanza: $amount")
    val events = mutableListOf<Event>(Event.BidPlaced(player, amount))
    val next = auction.copy(highestBid = amount, highestBidder = player)
    return Result(settle(config, state.copy(phase = next), events), events)
}

/** Se retira de la subasta; quien va ganando no se retira. */
internal fun passBid(config: GameConfig, state: GameState, player: Int): Result {
    val auction = state.phase as? TurnPhase.Auction ?: throw IllegalActionException("no hay subasta: ${state.phase}")
    if (player !in auction.bidders) throw IllegalActionException("ya no puja: $player")
    if (player == auction.highestBidder) throw IllegalActionException("va ganando: no se retira")
    val events = mutableListOf<Event>(Event.BidPassed(player))
    val next = auction.copy(bidders = auction.bidders - player)
    return Result(settle(config, state.copy(phase = next), events), events)
}

/** Base de la subasta: precio − `auctionPriceDiscount` (R-44), o $1 si no hay (R-12). */
fun minimumBid(config: GameConfig, square: Int): Int {
    val discount = config.rules.auctionPriceDiscount ?: return 1
    return maxOf(1, (config.squares[square] as OwnableSquare).price - discount)
}

/**
 * Cierra la subasta si ya se decidió: queda solo quien va ganando → la paga al Banco (R-12);
 * nadie pujó → sigue del Banco (D-08). Luego la siguiente de `queue`, o `then`.
 */
private fun settle(config: GameConfig, state: GameState, events: MutableList<Event>): GameState {
    val auction = state.phase as TurnPhase.Auction
    val winner = auction.highestBidder
    var after = when {
        winner != null && auction.bidders == listOf(winner) -> {
            events += Event.AuctionWon(winner, auction.square, auction.highestBid)
            state.addMoney(winner, -auction.highestBid)
                .copy(holdings = state.holdings + (auction.square to Holding(winner)))
        }
        auction.bidders.isEmpty() -> {
            events += Event.AuctionUnsold(auction.square)
            state
        }
        else -> return state
    }
    if (auction.queue.isNotEmpty()) {
        return startAuction(config, after, auction.queue.first(), events, auction.queue.drop(1), auction.then)
    }
    after = after.copy(phase = TurnPhase.Roll)
    return auction.then?.let { after.copy(phase = it) } ?: Engine.finishMove(config, after, events)
}
