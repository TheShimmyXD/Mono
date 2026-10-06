package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.TurnPhase

/**
 * Hipoteca `square` con el Banco (R-31, R-49): cobra su valor (`mortgageValue`). Sin
 * `mortgageWithBuildings`, antes hay que vender los edificios de todo el grupo (R-31).
 */
internal fun mortgage(config: GameConfig, state: GameState, square: Int): Result {
    val player = actor(state)
    val holding = ownedBy(state, square, player)
    if (holding.mortgaged) illegal("ya está hipotecada: $square")
    val sq = config.squares[square]
    if (!config.rules.mortgageWithBuildings && sq is Property &&
        groupSquares(config, sq.group).any { state.holdings[it].hasBuildings() }
    ) illegal("antes se venden los edificios del grupo (R-31)")
    val value = mortgageValue(config, state, square)
    val after = state.addMoney(player, value).copy(holdings = state.holdings + (square to holding.copy(mortgaged = true)))
    return Result(after, listOf(Event.Mortgaged(player, square, value)))
}

/**
 * Levanta la hipoteca: su valor más `unmortgageFee` (R-32: 10 %; R-50: $200). Si el interés
 * ya se pagó en este turno (`feePaid`, R-34), solo el valor, y su dueño la levanta aunque no
 * sea su turno (D-15).
 */
internal fun unmortgage(config: GameConfig, state: GameState, square: Int): Result {
    val received = state.holdings[square]?.takeIf { it.feePaid }
    val player = received?.owner ?: state.current
    if (state.phase != TurnPhase.Roll && state.phase != TurnPhase.EndOfTurn) illegal("no se levanta ahora: ${state.phase}")
    val holding = ownedBy(state, square, player)
    if (!holding.mortgaged) illegal("no está hipotecada: $square")
    val value = mortgageValue(config, state, square)
    val fee = config.rules.unmortgageFee
    val cost = value + if (holding.feePaid) 0 else percentOf(value, fee.percent, config.rules.percentRounding) + fee.fixed
    if (state.players[player].money < cost) illegal("no alcanza: $cost")
    val after = state.addMoney(player, -cost)
        .copy(holdings = state.holdings + (square to holding.copy(mortgaged = false, feePaid = false)))
    return Result(after, listOf(Event.Unmortgaged(player, square, cost)))
}

/**
 * Vende al Banco una casa o el hotel de `square` a la mitad de lo que costó (R-30), parejo
 * y al revés de como se construyó (R-26). El hotel vuelve a `maxHouses` casas si el Banco las
 * tiene; si no, se vende entero con ellas (R-30: «un hotel equivale a cinco casas»).
 * En una hipotecada no se venden (D-15).
 */
internal fun sellBuilding(config: GameConfig, state: GameState, square: Int): Result {
    val rules = config.rules
    if (!rules.sellBuildingsToBank) illegal("los edificios no se venden al Banco")
    val player = actor(state)
    val holding = ownedBy(state, square, player)
    if (holding.mortgaged) illegal("hipotecada: $square (D-15)")
    if (!holding.hasBuildings()) illegal("no tiene edificios: $square")
    val property = config.squares[square] as Property
    val group = groupSquares(config, property.group)
    if (rules.evenBuild && level(config, holding) < group.maxOf { level(config, state.holdings[it]) }) {
        illegal("hay que vender parejo (R-26)")
    }
    val house = rules.housePrice ?: property.housePrice ?: 0
    val limitedHouses = rules.houseStock > 0
    var after = state
    val (left, cost) = when {
        !holding.hotel -> {
            if (limitedHouses) after = after.copy(bankHouses = after.bankHouses + 1)
            holding.copy(houses = holding.houses - 1) to house
        }
        else -> {
            val hotel = rules.hotelPrice ?: property.hotelPrice ?: 0
            if (rules.hotelStock > 0) after = after.copy(bankHotels = after.bankHotels + 1)
            val breaksDown = rules.hotelReturnsHouses && (!limitedHouses || after.bankHouses >= rules.maxHouses)
            when {
                !rules.hotelReturnsHouses -> holding.copy(hotel = false) to hotel
                breaksDown -> {
                    if (limitedHouses) after = after.copy(bankHouses = after.bankHouses - rules.maxHouses)
                    holding.copy(hotel = false, houses = rules.maxHouses) to hotel
                }
                else -> holding.copy(hotel = false, houses = 0) to hotel + rules.maxHouses * house
            }
        }
    }
    val refund = percentOf(cost, 50, rules.percentRounding)
    after = after.addMoney(player, refund).copy(holdings = after.holdings + (square to left))
    return Result(after, listOf(Event.BuildingSold(player, square, refund)))
}

/** Valor de la hipoteca: el impreso (R-31) o la mitad del precio total con edificios (R-49). */
fun mortgageValue(config: GameConfig, state: GameState, square: Int): Int {
    val sq = config.squares[square] as OwnableSquare
    return when (config.rules.mortgageValue) {
        MortgageValue.PRINTED -> sq.mortgage ?: illegal("sin valor de hipoteca: $square")
        MortgageValue.HALF_TOTAL -> {
            val holding = state.holdings[square]
            val buildings = if (sq is Property && holding != null) buildingsCost(config, sq, holding) else 0
            percentOf(sq.price + buildings, 50, config.rules.percentRounding)
        }
    }
}

/**
 * Lo que costaron los edificios de un solar: sus casas y el hotel; con `hotelReturnsHouses`
 * el hotel lleva también las `maxHouses` casas que se entregaron (R-19, R-39, D-13).
 */
fun buildingsCost(config: GameConfig, property: Property, holding: Holding): Int {
    val rules = config.rules
    val house = rules.housePrice ?: property.housePrice ?: 0
    var total = holding.houses * house
    if (holding.hotel) {
        total += rules.hotelPrice ?: property.hotelPrice ?: 0
        if (rules.hotelReturnsHouses) total += rules.maxHouses * house
    }
    return total
}

/**
 * Quien hace la acción: quien juega, en las fases en que puede vender o hipotecar en su turno
 * (D-12, D-15), o quien debe, mientras junta para pagar (R-34).
 */
internal fun actor(state: GameState): Int = when (state.phase) {
    TurnPhase.Roll, TurnPhase.EndOfTurn, is TurnPhase.Buy, is TurnPhase.TaxChoice -> state.current
    is TurnPhase.Debt -> (state.phase as TurnPhase.Debt).debts.first().debtor
    else -> illegal("no se hipoteca ni se vende ahora: ${state.phase}")
}

/** El `Holding` de `square` si es de `player`. */
private fun ownedBy(state: GameState, square: Int, player: Int): Holding {
    val holding = state.holdings[square]
    if (holding == null || holding.owner != player) illegal("no es tuya: $square")
    return holding
}

/** Nivel de un solar para construir o vender parejo: casas, o `maxHouses` + 1 con hotel. */
private fun level(config: GameConfig, holding: Holding?): Int =
    if (holding == null) 0 else if (holding.hotel) config.rules.maxHouses + 1 else holding.houses

private fun Holding?.hasBuildings() = this != null && (houses > 0 || hotel)

private fun illegal(message: String): Nothing = throw IllegalActionException(message)
