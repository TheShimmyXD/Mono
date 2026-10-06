package com.jacck.mono.engine

import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.TurnPhase

/**
 * Construye en `square` una casa, o el hotel si ya tiene `maxHouses` (R-25..R-28, R-46, R-47).
 * Solo quien juega y en su turno (D-12); nunca en una hipotecada (R-49, D-12).
 */
internal fun build(config: GameConfig, state: GameState, square: Int): Result {
    val rules = config.rules
    val player = state.current
    val property = config.squares.getOrNull(square) as? Property ?: illegal("no es un solar: $square")
    val holding = state.holdings[square]
    if (holding == null || holding.owner != player) illegal("no es tuya: $square")
    if (state.phase != TurnPhase.Roll && state.phase != TurnPhase.EndOfTurn) illegal("no se construye ahora: ${state.phase}")
    if (holding.mortgaged) illegal("hipotecada: $square")
    val group = groupSquares(config, property.group)
    if (group.any { state.holdings[it]?.owner != player }) illegal("falta el grupo completo")
    if (rules.buildOnlyWhenLanding) {
        val movedThisTurn = state.phase == TurnPhase.EndOfTurn || state.doublesInRow > 0
        if (!movedThisTurn || state.players[player].position != square) illegal("solo al caer en ella (R-46)")
    }
    if (holding.hotel) illegal("ya tiene hotel")
    // Nivel de cada solar del grupo: casas, o maxHouses + 1 con hotel.
    val levels = group.map { state.holdings.getValue(it).let { h -> if (h.hotel) rules.maxHouses + 1 else h.houses } }
    if (rules.evenBuild && holding.houses > levels.min()) illegal("hay que construir parejo (R-26)")
    return if (holding.houses < rules.maxHouses) {
        addHouse(config, state, square, property, holding)
    } else {
        addHotel(config, state, square, property, holding)
    }
}

/** Una casa al precio de la regla o de la Escritura (R-25, R-46); el Banco debe tener (R-28). */
private fun addHouse(config: GameConfig, state: GameState, square: Int, property: Property, holding: Holding): Result {
    val limited = config.rules.houseStock > 0
    if (limited && state.bankHouses == 0) illegal("el Banco no tiene casas (R-28)")
    val price = config.rules.housePrice ?: property.housePrice ?: illegal("sin precio de casa: $square")
    val built = pay(state, price)
        .withHolding(square, holding.copy(houses = holding.houses + 1))
        .let { if (limited) it.copy(bankHouses = it.bankHouses - 1) else it }
    return Result(built, listOf(Event.HouseBuilt(state.current, square, holding.houses + 1, price)))
}

/**
 * El hotel o castillo (R-27, R-47): con `evenBuild`, todo el grupo con `maxHouses` (ya lo
 * aseguró `build`); sus casas vuelven al Banco si `hotelReturnsHouses` (D-08).
 */
private fun addHotel(config: GameConfig, state: GameState, square: Int, property: Property, holding: Holding): Result {
    val rules = config.rules
    if (rules.hotelStock > 0 && state.bankHotels == 0) illegal("el Banco no tiene hoteles (R-28)")
    val price = rules.hotelPrice ?: property.hotelPrice ?: illegal("sin precio de hotel: $square")
    var built = pay(state, price).withHolding(square, holding.copy(houses = 0, hotel = true))
    if (rules.hotelStock > 0) built = built.copy(bankHotels = built.bankHotels - 1)
    if (rules.hotelReturnsHouses && rules.houseStock > 0) built = built.copy(bankHouses = built.bankHouses + holding.houses)
    return Result(built, listOf(Event.HotelBuilt(state.current, square, price)))
}

/** Quien juega paga `price` al Banco, si le alcanza. */
private fun pay(state: GameState, price: Int): GameState {
    if (state.players[state.current].money < price) illegal("no alcanza: $price")
    return state.addMoney(state.current, -price)
}

private fun GameState.withHolding(square: Int, holding: Holding) = copy(holdings = holdings + (square to holding))

private fun illegal(message: String): Nothing = throw IllegalActionException(message)
