package com.jacck.mono.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jacck.mono.R
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.JailCause
import com.jacck.mono.engine.JailExit
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState

/**
 * Eventos que merecen un diálogo «Lo que pasó» (D-22): cobros y pagos que el jugador no eligió.
 * Las acciones que él mismo tocó (comprar, pujar, terminar) no se repiten.
 */
fun Event.isNotable(): Boolean = when (this) {
    is Event.FirstPlayer, is Event.SalaryPaid, is Event.RentPaid, is Event.TaxPaid, is Event.CardDrawn,
    is Event.SentToJail, is Event.StayedInJail, is Event.LeftJail, is Event.AuctionWon, is Event.AuctionUnsold,
    is Event.InDebt, is Event.Bankrupt, is Event.DeedDealt, is Event.GameOver, is Event.RollAgain -> true
    else -> false
}

@Composable
fun money(amount: Int): String = stringResource(R.string.money, amount)

/** Una línea en español para el evento, o null si no se cuenta (se ve en el tablero o lo hizo él). */
@Composable
fun eventLine(event: Event, config: GameConfig, state: GameState): String? {
    fun who(p: Int) = state.players[p].name
    fun sq(i: Int) = config.squares[i].name
    return when (event) {
        is Event.StartRolled -> stringResource(
            R.string.ev_start_rolled,
            event.rolls.entries.joinToString(", ") { (p, d) -> "${who(p)}: ${d.total}" },
        )
        is Event.FirstPlayer -> stringResource(R.string.ev_first_player, who(event.player))
        is Event.DiceRolled -> stringResource(
            R.string.ev_dice, who(event.player), event.dice.first, event.dice.second, event.dice.total,
        )
        is Event.Moved -> stringResource(R.string.ev_moved, who(event.player), sq(event.to))
        is Event.SalaryPaid -> stringResource(R.string.ev_salary, who(event.player), money(event.amount))
        is Event.Bought -> stringResource(R.string.ev_bought, who(event.player), sq(event.square), money(event.price))
        is Event.AuctionWon -> stringResource(R.string.ev_auction_won, who(event.player), sq(event.square), money(event.amount))
        is Event.AuctionUnsold -> stringResource(R.string.ev_auction_unsold, sq(event.square))
        is Event.RentPaid -> stringResource(R.string.ev_rent, who(event.payer), who(event.owner), sq(event.square), money(event.amount))
        is Event.TaxPaid -> stringResource(R.string.ev_tax, who(event.player), sq(event.square), money(event.amount))
        is Event.CardDrawn -> stringResource(R.string.ev_card, who(event.player), config.cards[event.card].text)
        is Event.CardPayment -> {
            val (from, to) = event.from to event.to
            when {
                from == null && to != null -> stringResource(R.string.ev_card_from_bank, who(to), money(event.amount))
                from != null && to == null -> stringResource(R.string.ev_card_to_bank, who(from), money(event.amount))
                from != null && to != null -> stringResource(R.string.ev_card_payment, who(from), who(to), money(event.amount))
                else -> null
            }
        }
        is Event.SentToJail -> if (event.cause == JailCause.DOUBLES) {
            stringResource(R.string.ev_jail_doubles, who(event.player), config.rules.doublesToJail)
        } else {
            stringResource(R.string.ev_jail, who(event.player))
        }
        is Event.StayedInJail -> stringResource(R.string.ev_stayed_jail, who(event.player), event.turns)
        is Event.LeftJail -> when (event.way) {
            JailExit.FINE, JailExit.LAST_TURN -> stringResource(R.string.ev_left_jail_fine, who(event.player), money(event.fine))
            else -> stringResource(R.string.ev_left_jail, who(event.player))
        }
        is Event.InDebt -> stringResource(R.string.ev_in_debt, who(event.debtor))
        is Event.Bankrupt -> stringResource(R.string.ev_bankrupt, who(event.player))
        is Event.DeedDealt -> stringResource(R.string.ev_deed_dealt, who(event.player), sq(event.square))
        is Event.GameOver -> stringResource(R.string.ev_game_over)
        is Event.RollAgain -> stringResource(R.string.ev_roll_again, who(event.player))
        else -> null
    }
}
