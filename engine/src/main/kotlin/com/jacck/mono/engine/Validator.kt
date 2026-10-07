package com.jacck.mono.engine

import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.NearestKind
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility

/**
 * Un error de la configuración: fuera de los rangos de D-09 o algo que el motor no resuelve
 * (D-12..D-14). El motor no lleva textos (D-02): la app lo convierte en un mensaje en español (D-16).
 * `square` y `card` son índices en `GameConfig.squares` y `GameConfig.cards`.
 */
sealed interface ConfigError {
    /** N fuera de 16..48 o no múltiplo de 4. */
    data class BoardSize(val size: Int) : ConfigError

    /** No hay exactamente una salida, en la casilla 0. */
    data object StartMisplaced : ConfigError

    /** No hay grupos de color. */
    data object NoGroups : ConfigError

    /** Dos grupos con el mismo `id`. */
    data class DuplicateGroup(val group: String) : ConfigError

    /** Grupo vacío o con más de 4 propiedades. */
    data class GroupSize(val group: String, val size: Int) : ConfigError

    /** Propiedad de un grupo que no existe. */
    data class UnknownGroup(val square: Int, val group: String) : ConfigError

    /** Nombre vacío o de más de 24 caracteres. */
    data class NameLength(val square: Int) : ConfigError

    /** Una cifra de la casilla fuera de 0..99 999 (el porcentaje, de 0..100). */
    data class SquareAmount(val square: Int, val field: SquareField) : ConfigError

    /** Falta una cifra que la regla deja a cada casilla (precio de casa u hotel, hipoteca). */
    data class MissingAmount(val square: Int, val field: SquareField) : ConfigError

    /** Alquileres de más o de menos: `expected` = `maxHouses` + 2, o uno por casilla del tipo. */
    data class RentCount(val square: Int, val expected: Int, val actual: Int) : ConfigError

    /** Una opción de reglas fuera de `min..max` (`doublesToJail` admite además 0). */
    data class RuleRange(val field: RuleField, val min: Int, val max: Int) : ConfigError

    /** Opciones que no casan entre sí o que el motor aún no tiene. */
    data class Incoherent(val reason: Incoherence) : ConfigError

    /** Casilla de carta cuyo mazo no tiene cartas (D-14). */
    data class EmptyDeck(val square: Int, val deck: Deck) : ConfigError

    /** Carta con un efecto imposible en este tablero (D-14). */
    data class BadCard(val card: Int, val problem: CardProblem) : ConfigError
}

/** Cifra de una casilla (D-09). */
enum class SquareField { PRICE, RENT, HOUSE_PRICE, HOTEL_PRICE, MORTGAGE, TAX_FIXED, TAX_PERCENT, TAX_PER_HOTEL }

/** Opción de reglas con rango (D-09; filas de `## Diferencias`). */
enum class RuleField {
    PLAYERS, STARTING_MONEY, SALARY, DOUBLES_TO_JAIL, JAIL_FINE, JAIL_MAX_TURNS, AUCTION_DISCOUNT,
    UNMORTGAGE_FEE, UNMORTGAGE_FEE_PERCENT, MAX_HOUSES, HOUSE_PRICE, HOTEL_PRICE, HOUSE_STOCK,
    HOTEL_STOCK, TRADE_FEE, MORTGAGED_TRADE_INTEREST, BANKRUPTCY_INTEREST, STARTING_DEEDS,
}

/** Por qué no casan las opciones. */
enum class Incoherence {
    /** `minPlayers` > `maxPlayers`. */
    PLAYERS_ORDER,
    /** `rentMustBeClaimed` = sí: el motor no lo tiene (R-17, D-12). */
    RENT_MUST_BE_CLAIMED,
    /** `freeParkingPot` = sí: el motor no lo tiene (R-24, D-13). */
    FREE_PARKING_POT,
    /** Sin Cárcel, pero hay casillas, cartas o `doublesToJail` que mandan a ella (D-09, D-13). */
    JAIL_DISABLED_BUT_USED,
    /** Con Cárcel, no hay exactamente una casilla Cárcel (D-13). */
    JAIL_SQUARE_COUNT,
}

/** Qué le falla a una carta (D-14). */
enum class CardProblem { MOVE_OUTSIDE_BOARD, MOVE_TO_CARD, NO_NEAREST, AMOUNT }

private const val MAX_AMOUNT = 99_999
const val MAX_SQUARE_NAME = 24
private val BOARD_SIZES = 16..48
private const val MAX_GROUP = 4

/** Todos los errores de `config`; vacío si se puede jugar (F2.7, D-09). */
fun validate(config: GameConfig): List<ConfigError> {
    val errors = mutableListOf<ConfigError>()
    errors += boardErrors(config)
    config.squares.indices.forEach { errors += squareErrors(config, it) }
    errors += ruleErrors(config)
    errors += cardErrors(config)
    return errors
}

/** Tamaño, salida y grupos (D-09). */
private fun boardErrors(config: GameConfig): List<ConfigError> {
    val errors = mutableListOf<ConfigError>()
    val squares = config.squares
    if (squares.size !in BOARD_SIZES || squares.size % 4 != 0) errors += ConfigError.BoardSize(squares.size)
    if (squares.count { it is Start } != 1 || squares.firstOrNull() !is Start) errors += ConfigError.StartMisplaced
    if (config.groups.isEmpty()) errors += ConfigError.NoGroups
    val ids = config.groups.map { it.id }
    ids.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach { errors += ConfigError.DuplicateGroup(it) }
    for (id in ids.distinct()) {
        val size = squares.count { it is Property && it.group == id }
        if (size !in 1..MAX_GROUP) errors += ConfigError.GroupSize(id, size)
    }
    squares.forEachIndexed { i, sq -> if (sq is Property && sq.group !in ids) errors += ConfigError.UnknownGroup(i, sq.group) }
    return errors
}

/** Nombre, cifras y número de alquileres de la casilla `i` (D-09). */
private fun squareErrors(config: GameConfig, i: Int): List<ConfigError> {
    val errors = mutableListOf<ConfigError>()
    val rules = config.rules
    val sq = config.squares[i]
    fun amount(value: Int?, field: SquareField, required: Boolean, max: Int = MAX_AMOUNT) {
        when {
            value == null -> if (required) errors += ConfigError.MissingAmount(i, field)
            value !in 0..max -> errors += ConfigError.SquareAmount(i, field)
        }
    }
    fun rentsInRange(values: List<Int>) {
        if (values.any { it !in 0..MAX_AMOUNT }) errors += ConfigError.SquareAmount(i, SquareField.RENT)
    }
    if (sq.name.length !in 1..MAX_SQUARE_NAME) errors += ConfigError.NameLength(i)
    if (sq is OwnableSquare) {
        amount(sq.price, SquareField.PRICE, required = true)
        amount(sq.mortgage, SquareField.MORTGAGE, required = rules.mortgageValue == MortgageValue.PRINTED)
    }
    when (sq) {
        is Property -> {
            amount(sq.housePrice, SquareField.HOUSE_PRICE, required = rules.housePrice == null)
            amount(sq.hotelPrice, SquareField.HOTEL_PRICE, required = rules.hotelPrice == null)
            rentsInRange(sq.rents)
            val expected = rules.maxHouses + 2
            if (sq.rents.size != expected) errors += ConfigError.RentCount(i, expected, sq.rents.size)
        }
        is Station -> {
            rentsInRange(sq.rents)
            val expected = config.squares.count { it is Station }
            if (sq.rents.size < expected) errors += ConfigError.RentCount(i, expected, sq.rents.size)
        }
        is Utility -> {
            rentsInRange(sq.diceMultipliers)
            val expected = config.squares.count { it is Utility }
            if (sq.diceMultipliers.size < expected) errors += ConfigError.RentCount(i, expected, sq.diceMultipliers.size)
        }
        is Tax -> {
            amount(sq.fixed, SquareField.TAX_FIXED, required = true)
            amount(sq.percent, SquareField.TAX_PERCENT, required = true, max = 100)
            amount(sq.perHotel, SquareField.TAX_PER_HOTEL, required = true)
        }
        else -> Unit
    }
    return errors
}

/** Rangos de las opciones (D-09) y lo que no casa entre ellas (D-12, D-13). */
private fun ruleErrors(config: GameConfig): List<ConfigError> {
    val errors = mutableListOf<ConfigError>()
    val r = config.rules
    fun range(value: Int?, field: RuleField, min: Int = 0, max: Int = MAX_AMOUNT) {
        if (value != null && value !in min..max) errors += ConfigError.RuleRange(field, min, max)
    }
    if (r.minPlayers !in 2..6 || r.maxPlayers !in 2..6) errors += ConfigError.RuleRange(RuleField.PLAYERS, 2, 6)
    range(r.startingMoney, RuleField.STARTING_MONEY)
    range(r.salary, RuleField.SALARY)
    if (r.doublesToJail != 0) range(r.doublesToJail, RuleField.DOUBLES_TO_JAIL, 2, 5)
    range(r.jailFine, RuleField.JAIL_FINE)
    range(r.jailMaxTurns, RuleField.JAIL_MAX_TURNS, 1, 5)
    range(r.auctionPriceDiscount, RuleField.AUCTION_DISCOUNT)
    range(r.unmortgageFee.fixed, RuleField.UNMORTGAGE_FEE)
    range(r.unmortgageFee.percent, RuleField.UNMORTGAGE_FEE_PERCENT, max = 100)
    range(r.maxHouses, RuleField.MAX_HOUSES, 1, 4)
    range(r.housePrice, RuleField.HOUSE_PRICE)
    range(r.hotelPrice, RuleField.HOTEL_PRICE)
    range(r.houseStock, RuleField.HOUSE_STOCK, max = 99)
    range(r.hotelStock, RuleField.HOTEL_STOCK, max = 99)
    range(r.tradeFee, RuleField.TRADE_FEE)
    range(r.mortgagedTradeInterest, RuleField.MORTGAGED_TRADE_INTEREST, max = 100)
    range(r.bankruptcyInterest, RuleField.BANKRUPTCY_INTEREST, max = 100)
    // Las Escrituras del juego corto tienen que alcanzar para todos (R-37, R-40).
    val deeds = config.squares.count { it is OwnableSquare }
    range(r.startingDeeds, RuleField.STARTING_DEEDS, max = deeds / maxOf(1, r.maxPlayers))

    if (r.minPlayers > r.maxPlayers) errors += ConfigError.Incoherent(Incoherence.PLAYERS_ORDER)
    if (r.rentMustBeClaimed) errors += ConfigError.Incoherent(Incoherence.RENT_MUST_BE_CLAIMED)
    if (r.freeParkingPot) errors += ConfigError.Incoherent(Incoherence.FREE_PARKING_POT)
    val jails = config.squares.count { it is Jail }
    val sendsToJail = r.doublesToJail > 0 || jails > 0 || config.squares.any { it is GoToJail } ||
        config.cards.any { it.effect == CardEffect.GoToJail || it.effect == CardEffect.GetOutOfJail }
    if (!r.jail && sendsToJail) errors += ConfigError.Incoherent(Incoherence.JAIL_DISABLED_BUT_USED)
    if (r.jail && jails != 1) errors += ConfigError.Incoherent(Incoherence.JAIL_SQUARE_COUNT)
    return errors
}

/** Mazos de las casillas de carta y efectos de cada carta (D-14). */
private fun cardErrors(config: GameConfig): List<ConfigError> {
    val errors = mutableListOf<ConfigError>()
    val squares = config.squares
    squares.forEachIndexed { i, sq ->
        if (sq is CardSquare && config.cards.none { it.deck == sq.deck }) errors += ConfigError.EmptyDeck(i, sq.deck)
    }
    config.cards.forEachIndexed { i, card ->
        fun bad(problem: CardProblem) { errors += ConfigError.BadCard(i, problem) }
        fun money(vararg values: Int) { if (values.any { it !in 0..MAX_AMOUNT }) bad(CardProblem.AMOUNT) }
        when (val e = card.effect) {
            is CardEffect.MoveTo -> when {
                e.square !in squares.indices -> bad(CardProblem.MOVE_OUTSIDE_BOARD)
                squares[e.square] is CardSquare -> bad(CardProblem.MOVE_TO_CARD)
            }
            is CardEffect.MoveToNearest -> {
                val present = squares.any { if (e.kind == NearestKind.STATION) it is Station else it is Utility }
                if (!present) bad(CardProblem.NO_NEAREST)
                if (e.rentFactor !in 1..MAX_AMOUNT || (e.diceMultiplier ?: 1) !in 1..MAX_AMOUNT) bad(CardProblem.AMOUNT)
            }
            is CardEffect.Collect -> money(e.amount)
            is CardEffect.Pay -> money(e.amount)
            is CardEffect.Repairs -> money(e.perHouse, e.perHotel)
            is CardEffect.CollectFromEach -> money(e.amount)
            is CardEffect.PayEach -> money(e.amount)
            is CardEffect.MoveBy, CardEffect.GoToJail, CardEffect.GetOutOfJail -> Unit
        }
    }
    return errors
}
