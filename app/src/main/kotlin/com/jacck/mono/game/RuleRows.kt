package com.jacck.mono.game

import com.jacck.mono.R
import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.Rounding
import com.jacck.mono.engine.model.RuleOptions
import com.jacck.mono.engine.model.StartTieRule

/**
 * Las reglas del editor (F4.2, D-40, maqueta B): una fila por opción de `RuleOptions` que el juego
 * ya usa, agrupadas en pestañas. Fuera quedan las que el teléfono aún no tiene: jugadores (se eligen
 * en el menú), tratos entre jugadores (`tradeBuildings`, `tradeFee`, `mortgagedTradeInterest`), fin
 * por tiempo, reclamar el alquiler y el bote de Parada Libre (el validador los rechaza, D-12, D-13),
 * el Banco sin dinero y la propiedad sin pujas (el motor no los distingue) y si hay Cárcel (depende
 * del tablero, F4.3). Los rangos los pone el validador del motor (D-09); aquí solo el − / +.
 */
enum class RuleTab(val label: Int) {
    DINERO(R.string.rules_tab_money), DADOS(R.string.rules_tab_dice), CASAS(R.string.rules_tab_houses),
    ALQUILER(R.string.rules_tab_rent), HIPOTECAS(R.string.rules_tab_mortgage), FIN(R.string.rules_tab_end),
}

/** Cómo se escribe y cuánto avanza una cifra. */
enum class NumberKind { MONEY, COUNT, PERCENT }

/** Una regla del editor: su pestaña, su nombre y la línea que la explica (ids de `strings.xml`). */
sealed interface RuleRow {
    val tab: RuleTab
    val label: Int
    val help: Int
}

/** Una opción entre varias: [isOn] dice si es la que tienen las reglas y [apply] la pone. */
class Choice(val label: Int, val isOn: (RuleOptions) -> Boolean, val apply: (RuleOptions) -> RuleOptions)

/** Regla de opciones (Sí / No o una lista). */
class ChoiceRow(override val tab: RuleTab, override val label: Int, override val help: Int, val choices: List<Choice>) : RuleRow

/**
 * Regla con cifra y − / +. [values] fija los valores posibles (los dobles: nunca, 2..5); [zero]
 * nombra el 0 («Sin límite»); con [optional], la cifra puede faltar (null = la de cada casilla).
 */
class NumberRow(
    override val tab: RuleTab, override val label: Int, override val help: Int,
    val kind: NumberKind, val range: IntRange,
    val get: (RuleOptions) -> Int?, val set: (RuleOptions, Int?) -> RuleOptions,
    val values: List<Int>? = null, val zero: Int? = null, val optional: Optional? = null,
) : RuleRow {

    /** La cifra un paso arriba o abajo, sin salir de [range] ni de [values]. */
    fun step(value: Int, up: Boolean): Int {
        values?.let { vs ->
            val i = vs.indexOf(value).takeIf { it >= 0 } ?: vs.indexOfFirst { it >= value }.coerceAtLeast(0)
            return vs[(if (up) i + 1 else i - 1).coerceIn(vs.indices)]
        }
        val size = when (kind) {
            NumberKind.COUNT -> 1
            NumberKind.PERCENT -> 5
            NumberKind.MONEY -> moneyStep(if (up) value else value - 1)
        }
        return (if (up) value + size else value - size).coerceIn(range)
    }

    /** Las reglas con la cifra un paso arriba o abajo; sin cifra (opcional apagada), igual. */
    fun step(rules: RuleOptions, up: Boolean): RuleOptions = get(rules)?.let { set(rules, step(it, up)) } ?: rules
}

/** Una cifra que puede faltar: [off] la deja en null, [on] pone [fallback]. */
class Optional(val off: Int, val on: Int, val fallback: Int)

/** Paso del dinero: de $10 bajo $100, de $50 bajo $1000, de $100 bajo $10 000 y de $1000 desde ahí. */
fun moneyStep(value: Int): Int = when {
    value < 100 -> 10
    value < 1000 -> 50
    value < 10_000 -> 100
    else -> 1000
}

private fun yesNo(get: (RuleOptions) -> Boolean, set: (RuleOptions, Boolean) -> RuleOptions) = listOf(
    Choice(R.string.rules_yes, get) { set(it, true) },
    Choice(R.string.rules_no, { !get(it) }) { set(it, false) },
)

private fun <E> pick(label: Int, value: E, get: (RuleOptions) -> E, set: (RuleOptions, E) -> RuleOptions) =
    Choice(label, { get(it) == value }) { set(it, value) }

private const val MAX = 99_999

/** Todas las filas, en el orden de cada pestaña. */
val RULE_ROWS: List<RuleRow> = listOf(
    NumberRow(RuleTab.DINERO, R.string.rules_starting_money, R.string.rules_starting_money_help, NumberKind.MONEY, 0..MAX,
        { it.startingMoney }, { r, v -> r.copy(startingMoney = v ?: r.startingMoney) }),
    NumberRow(RuleTab.DINERO, R.string.rules_salary, R.string.rules_salary_help, NumberKind.MONEY, 0..MAX,
        { it.salary }, { r, v -> r.copy(salary = v ?: r.salary) }),
    NumberRow(RuleTab.DINERO, R.string.rules_auction, R.string.rules_auction_help, NumberKind.MONEY, 0..MAX,
        { it.auctionPriceDiscount }, { r, v -> r.copy(auctionPriceDiscount = v) },
        optional = Optional(R.string.rules_auction_any, R.string.rules_auction_discount, 200)),

    ChoiceRow(RuleTab.DADOS, R.string.rules_doubles_again, R.string.rules_doubles_again_help,
        yesNo({ it.doublesRollAgain }) { r, v -> r.copy(doublesRollAgain = v) }),
    NumberRow(RuleTab.DADOS, R.string.rules_doubles_jail, R.string.rules_doubles_jail_help, NumberKind.COUNT, 0..5,
        { it.doublesToJail }, { r, v -> r.copy(doublesToJail = v ?: r.doublesToJail) },
        values = listOf(0, 2, 3, 4, 5), zero = R.string.rules_never),
    ChoiceRow(RuleTab.DADOS, R.string.rules_tie, R.string.rules_tie_help, listOf(
        pick(R.string.rules_tie_tied, StartTieRule.REROLL_TIED, { it.startTieRule }) { r, v -> r.copy(startTieRule = v) },
        pick(R.string.rules_tie_all, StartTieRule.REROLL_ALL, { it.startTieRule }) { r, v -> r.copy(startTieRule = v) },
    )),
    NumberRow(RuleTab.DADOS, R.string.rules_jail_fine, R.string.rules_jail_fine_help, NumberKind.MONEY, 0..MAX,
        { it.jailFine }, { r, v -> r.copy(jailFine = v ?: r.jailFine) }),
    NumberRow(RuleTab.DADOS, R.string.rules_jail_turns, R.string.rules_jail_turns_help, NumberKind.COUNT, 1..5,
        { it.jailMaxTurns }, { r, v -> r.copy(jailMaxTurns = v ?: r.jailMaxTurns) }),

    NumberRow(RuleTab.CASAS, R.string.rules_max_houses, R.string.rules_max_houses_help, NumberKind.COUNT, 1..4,
        { it.maxHouses }, { r, v -> r.copy(maxHouses = v ?: r.maxHouses) }),
    ChoiceRow(RuleTab.CASAS, R.string.rules_even_build, R.string.rules_even_build_help,
        yesNo({ it.evenBuild }) { r, v -> r.copy(evenBuild = v) }),
    ChoiceRow(RuleTab.CASAS, R.string.rules_build_landing, R.string.rules_build_landing_help,
        yesNo({ it.buildOnlyWhenLanding }) { r, v -> r.copy(buildOnlyWhenLanding = v) }),
    NumberRow(RuleTab.CASAS, R.string.rules_house_price, R.string.rules_house_price_help, NumberKind.MONEY, 0..MAX,
        { it.housePrice }, { r, v -> r.copy(housePrice = v) },
        optional = Optional(R.string.rules_each_square, R.string.rules_fixed, 100)),
    NumberRow(RuleTab.CASAS, R.string.rules_hotel_price, R.string.rules_hotel_price_help, NumberKind.MONEY, 0..MAX,
        { it.hotelPrice }, { r, v -> r.copy(hotelPrice = v) },
        optional = Optional(R.string.rules_each_square, R.string.rules_fixed, 100)),
    ChoiceRow(RuleTab.CASAS, R.string.rules_hotel_returns, R.string.rules_hotel_returns_help,
        yesNo({ it.hotelReturnsHouses }) { r, v -> r.copy(hotelReturnsHouses = v) }),
    ChoiceRow(RuleTab.CASAS, R.string.rules_sell_bank, R.string.rules_sell_bank_help,
        yesNo({ it.sellBuildingsToBank }) { r, v -> r.copy(sellBuildingsToBank = v) }),
    NumberRow(RuleTab.CASAS, R.string.rules_house_stock, R.string.rules_house_stock_help, NumberKind.COUNT, 0..99,
        { it.houseStock }, { r, v -> r.copy(houseStock = v ?: r.houseStock) }, zero = R.string.rules_no_limit),
    NumberRow(RuleTab.CASAS, R.string.rules_hotel_stock, R.string.rules_hotel_stock_help, NumberKind.COUNT, 0..99,
        { it.hotelStock }, { r, v -> r.copy(hotelStock = v ?: r.hotelStock) }, zero = R.string.rules_no_limit),

    ChoiceRow(RuleTab.ALQUILER, R.string.rules_group_double, R.string.rules_group_double_help,
        yesNo({ it.groupDoubleRent }) { r, v -> r.copy(groupDoubleRent = v) }),
    ChoiceRow(RuleTab.ALQUILER, R.string.rules_hotel_group_double, R.string.rules_hotel_group_double_help,
        yesNo({ it.hotelGroupDoubleRent }) { r, v -> r.copy(hotelGroupDoubleRent = v) }),
    ChoiceRow(RuleTab.ALQUILER, R.string.rules_rent_mortgaged, R.string.rules_rent_mortgaged_help,
        yesNo({ it.rentWhileMortgaged }) { r, v -> r.copy(rentWhileMortgaged = v) }),

    ChoiceRow(RuleTab.HIPOTECAS, R.string.rules_mortgage_value, R.string.rules_mortgage_value_help, listOf(
        pick(R.string.rules_mortgage_printed, MortgageValue.PRINTED, { it.mortgageValue }) { r, v -> r.copy(mortgageValue = v) },
        pick(R.string.rules_mortgage_half, MortgageValue.HALF_TOTAL, { it.mortgageValue }) { r, v -> r.copy(mortgageValue = v) },
    )),
    ChoiceRow(RuleTab.HIPOTECAS, R.string.rules_mortgage_buildings, R.string.rules_mortgage_buildings_help,
        yesNo({ it.mortgageWithBuildings }) { r, v -> r.copy(mortgageWithBuildings = v) }),
    NumberRow(RuleTab.HIPOTECAS, R.string.rules_unmortgage_percent, R.string.rules_unmortgage_percent_help, NumberKind.PERCENT, 0..100,
        { it.unmortgageFee.percent }, { r, v -> r.copy(unmortgageFee = r.unmortgageFee.copy(percent = v ?: 0)) }),
    NumberRow(RuleTab.HIPOTECAS, R.string.rules_unmortgage_fixed, R.string.rules_unmortgage_fixed_help, NumberKind.MONEY, 0..MAX,
        { it.unmortgageFee.fixed }, { r, v -> r.copy(unmortgageFee = r.unmortgageFee.copy(fixed = v ?: 0)) }),
    ChoiceRow(RuleTab.HIPOTECAS, R.string.rules_rounding, R.string.rules_rounding_help, listOf(
        pick(R.string.rules_rounding_up, Rounding.UP, { it.percentRounding }) { r, v -> r.copy(percentRounding = v) },
        pick(R.string.rules_rounding_down, Rounding.DOWN, { it.percentRounding }) { r, v -> r.copy(percentRounding = v) },
        pick(R.string.rules_rounding_nearest, Rounding.NEAREST, { it.percentRounding }) { r, v -> r.copy(percentRounding = v) },
    )),

    ChoiceRow(RuleTab.FIN, R.string.rules_end, R.string.rules_end_help, listOf(
        pick(R.string.rules_end_last, EndCondition.LAST_STANDING, { it.endCondition }) { r, v -> r.copy(endCondition = v) },
        pick(R.string.rules_end_second, EndCondition.SECOND_BANKRUPTCY, { it.endCondition }) { r, v -> r.copy(endCondition = v) },
    )),
    NumberRow(RuleTab.FIN, R.string.rules_bankruptcy_interest, R.string.rules_bankruptcy_interest_help, NumberKind.PERCENT, 0..100,
        { it.bankruptcyInterest }, { r, v -> r.copy(bankruptcyInterest = v ?: r.bankruptcyInterest) }),
    NumberRow(RuleTab.FIN, R.string.rules_starting_deeds, R.string.rules_starting_deeds_help, NumberKind.COUNT, 0..6,
        { it.startingDeeds }, { r, v -> r.copy(startingDeeds = v ?: r.startingDeeds) }),
)
