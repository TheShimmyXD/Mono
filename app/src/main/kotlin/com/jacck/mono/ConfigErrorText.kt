package com.jacck.mono

import android.content.res.Resources
import com.jacck.mono.engine.CardProblem
import com.jacck.mono.engine.ConfigError
import com.jacck.mono.engine.Incoherence
import com.jacck.mono.engine.RuleField
import com.jacck.mono.engine.SquareField
import com.jacck.mono.engine.model.GameConfig

/**
 * Mensaje en español de un error del validador (F2.7, D-16). El `when` cubre todos los casos:
 * un error nuevo en el motor sin su mensaje no compila.
 */
fun ConfigError.message(res: Resources, config: GameConfig): String {
    fun square(i: Int) = res.getString(R.string.square_label, config.squares.getOrNull(i)?.name.orEmpty(), i)
    return when (this) {
        is ConfigError.BoardSize -> res.getString(R.string.error_board_size, size)
        ConfigError.StartMisplaced -> res.getString(R.string.error_start_misplaced)
        ConfigError.NoGroups -> res.getString(R.string.error_no_groups)
        is ConfigError.DuplicateGroup -> res.getString(R.string.error_duplicate_group, group)
        is ConfigError.GroupSize -> res.getString(R.string.error_group_size, group, size)
        is ConfigError.UnknownGroup -> res.getString(R.string.error_unknown_group, square(square), group)
        is ConfigError.NameLength -> res.getString(R.string.error_name_length, square(square))
        is ConfigError.SquareAmount -> res.getString(R.string.error_square_amount, square(square), res.getString(field.label()))
        is ConfigError.MissingAmount -> res.getString(R.string.error_missing_amount, square(square), res.getString(field.label()))
        is ConfigError.RentCount -> res.getString(R.string.error_rent_count, square(square), expected, actual)
        is ConfigError.RuleRange ->
            if (field == RuleField.DOUBLES_TO_JAIL) res.getString(R.string.error_doubles_range)
            else res.getString(R.string.error_rule_range, res.getString(field.label()), min, max)
        is ConfigError.Incoherent -> res.getString(reason.label())
        is ConfigError.EmptyDeck -> res.getString(R.string.error_empty_deck, square(square), deck.name)
        is ConfigError.BadCard -> res.getString(R.string.error_bad_card, card + 1, res.getString(problem.label()))
    }
}

private fun SquareField.label(): Int = when (this) {
    SquareField.PRICE -> R.string.field_price
    SquareField.RENT -> R.string.field_rent
    SquareField.HOUSE_PRICE -> R.string.field_house_price
    SquareField.HOTEL_PRICE -> R.string.field_hotel_price
    SquareField.MORTGAGE -> R.string.field_mortgage
    SquareField.TAX_FIXED -> R.string.field_tax_fixed
    SquareField.TAX_PERCENT -> R.string.field_tax_percent
    SquareField.TAX_PER_HOTEL -> R.string.field_tax_per_hotel
}

private fun RuleField.label(): Int = when (this) {
    RuleField.PLAYERS -> R.string.rule_players
    RuleField.STARTING_MONEY -> R.string.rule_starting_money
    RuleField.SALARY -> R.string.rule_salary
    RuleField.DOUBLES_TO_JAIL -> R.string.rule_doubles_to_jail
    RuleField.JAIL_FINE -> R.string.rule_jail_fine
    RuleField.JAIL_MAX_TURNS -> R.string.rule_jail_max_turns
    RuleField.AUCTION_DISCOUNT -> R.string.rule_auction_discount
    RuleField.UNMORTGAGE_FEE -> R.string.rule_unmortgage_fee
    RuleField.UNMORTGAGE_FEE_PERCENT -> R.string.rule_unmortgage_fee_percent
    RuleField.MAX_HOUSES -> R.string.rule_max_houses
    RuleField.HOUSE_PRICE -> R.string.rule_house_price
    RuleField.HOTEL_PRICE -> R.string.rule_hotel_price
    RuleField.HOUSE_STOCK -> R.string.rule_house_stock
    RuleField.HOTEL_STOCK -> R.string.rule_hotel_stock
    RuleField.TRADE_FEE -> R.string.rule_trade_fee
    RuleField.MORTGAGED_TRADE_INTEREST -> R.string.rule_mortgaged_trade_interest
    RuleField.BANKRUPTCY_INTEREST -> R.string.rule_bankruptcy_interest
    RuleField.STARTING_DEEDS -> R.string.rule_starting_deeds
}

private fun Incoherence.label(): Int = when (this) {
    Incoherence.PLAYERS_ORDER -> R.string.incoherent_players_order
    Incoherence.RENT_MUST_BE_CLAIMED -> R.string.incoherent_rent_must_be_claimed
    Incoherence.FREE_PARKING_POT -> R.string.incoherent_free_parking_pot
    Incoherence.JAIL_DISABLED_BUT_USED -> R.string.incoherent_jail_disabled_but_used
    Incoherence.JAIL_SQUARE_COUNT -> R.string.incoherent_jail_square_count
}

private fun CardProblem.label(): Int = when (this) {
    CardProblem.MOVE_OUTSIDE_BOARD -> R.string.card_move_outside_board
    CardProblem.MOVE_TO_CARD -> R.string.card_move_to_card
    CardProblem.NO_NEAREST -> R.string.card_no_nearest
    CardProblem.AMOUNT -> R.string.card_amount
}
