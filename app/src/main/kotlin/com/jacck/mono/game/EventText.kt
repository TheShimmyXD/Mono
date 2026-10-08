package com.jacck.mono.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jacck.mono.R
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.JailCause
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState

/**
 * Eventos que merecen un diálogo «Lo que pasó» (D-22): cobros y pagos que el jugador no eligió.
 * Las acciones que él mismo tocó (comprar, pujar, terminar) no se repiten.
 */
fun Event.isNotable(): Boolean = when (this) {
    is Event.FirstPlayer, is Event.CardDrawn,
    is Event.SentToJail, is Event.StayedInJail, is Event.LeftJail, is Event.AuctionUnsold,
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
        is Event.Moved -> null // la ficha camina hasta allá (FC.1, D-63)
        is Event.SalaryPaid, is Event.TaxPaid -> null // los billetes van entre el Banco y el ícono (FD.3, D-68)
        is Event.Bought, is Event.AuctionWon -> null // la casilla vuela al ícono de quien se la queda (FD.1, D-65)
        is Event.AuctionUnsold -> stringResource(R.string.ev_auction_unsold, sq(event.square))
        is Event.RentPaid -> null // los billetes van de un ícono al otro (FD.2, D-66)
        is Event.CardDrawn -> stringResource(R.string.ev_card, who(event.player), config.cards[event.card].text)
        is Event.CardPayment -> null // los billetes van de quien paga a quien cobra, jugador o Banco (FD.2, FD.3)
        is Event.SentToJail -> if (event.cause == JailCause.DOUBLES) {
            stringResource(R.string.ev_jail_doubles, who(event.player), config.rules.doublesToJail)
        } else {
            stringResource(R.string.ev_jail, who(event.player))
        }
        is Event.StayedInJail -> stringResource(R.string.ev_stayed_jail, who(event.player), event.turns)
        // Con multa, los billetes van al Banco (FD.3, D-68).
        is Event.LeftJail -> if (event.fine > 0) null else stringResource(R.string.ev_left_jail, who(event.player))
        is Event.InDebt -> stringResource(R.string.ev_in_debt, who(event.debtor))
        is Event.Bankrupt -> stringResource(R.string.ev_bankrupt, who(event.player))
        is Event.DeedDealt -> stringResource(R.string.ev_deed_dealt, who(event.player), sq(event.square))
        is Event.GameOver -> stringResource(R.string.ev_game_over)
        is Event.RollAgain -> stringResource(R.string.ev_roll_again, who(event.player))
        else -> null
    }
}
