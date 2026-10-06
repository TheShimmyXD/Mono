package com.jacck.mono.engine

import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.MortgageValue
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.PlayerDebt
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.TurnPhase

/**
 * Tras cada acción: quien quedó con saldo negativo entra en deuda con quien le cobró por
 * última vez (o con el Banco), y quien ya juntó para pagar sale de ella (R-34, R-35).
 * Mientras haya deudas, solo el primer deudor vende, hipoteca o se declara en quiebra.
 */
internal fun openDebts(state: GameState, events: MutableList<Event>): GameState {
    val phase = state.phase
    if (phase is TurnPhase.Over) return state
    val debt = phase as? TurnPhase.Debt
    val open = debt?.debts.orEmpty().filter { state.players[it.debtor].money < 0 && !state.players[it.debtor].bankrupt }
    val count = state.players.size
    val fresh = (0 until count).map { (state.current + it) % count }
        .filter { p -> state.players[p].money < 0 && !state.players[p].bankrupt && open.none { it.debtor == p } }
        .map { p -> creditorOf(p, events).also { events += Event.InDebt(p, it.creditor) } }
    val debts = open + fresh
    val resume = debt?.resume ?: phase
    return state.copy(phase = if (debts.isEmpty()) resume else TurnPhase.Debt(debts, resume))
}

/** A quién debe `player`: al último que le cobró en la acción; impuestos, multas y lo demás, al Banco. */
private fun creditorOf(player: Int, events: List<Event>): PlayerDebt {
    for (e in events.asReversed()) when (e) {
        is Event.RentPaid -> if (e.payer == player) return PlayerDebt(player, e.owner, e.amount)
        is Event.CardPayment -> if (e.from == player) return PlayerDebt(player, e.to, if (e.to == null) 0 else e.amount)
        is Event.TaxPaid -> if (e.player == player) return PlayerDebt(player, null)
        else -> Unit
    }
    return PlayerDebt(player, null)
}

/**
 * El primer deudor se declara en quiebra, solo si ni vendiendo edificios ni hipotecando alcanza.
 * Ante un jugador (R-34) o el Banco (R-35); con `SECOND_BANKRUPTCY`, la segunda quiebra entrega
 * todo entero y termina la partida (R-39). Si quien quiebra es quien juega, pasa el turno.
 */
internal fun declareBankruptcy(config: GameConfig, state: GameState): Result {
    val phase = state.phase as? TurnPhase.Debt ?: throw IllegalActionException("no hay deuda: ${state.phase}")
    val debt = phase.debts.first()
    val player = debt.debtor
    if (state.players[player].money + raisable(config, state, player) >= 0) {
        throw IllegalActionException("aún alcanza vendiendo o hipotecando (R-34)")
    }
    val events = mutableListOf<Event>(Event.Bankrupt(player, debt.creditor))
    val whole = config.rules.endCondition == EndCondition.SECOND_BANKRUPTCY && state.players.any { it.bankrupt }
    val owned = state.holdings.filterValues { it.owner == player }.keys.sorted()
    val standing = state.players.indices.filter { it != player && !state.players[it].bankrupt }
    val over = whole || standing.size == 1
    var after = state
    var resume = phase.resume
    // Antes del traspaso: pasar el turno borra `feePaid` (R-34).
    if (!over && player == state.current) {
        after = Engine.passTurn(after, events)
        resume = TurnPhase.Roll
    }
    after = if (debt.creditor != null) toPlayer(config, after, debt, owned, whole) else toBank(config, after, player, owned)
    val players = after.players.toMutableList()
    players[player] = players[player].copy(money = 0, jailTurns = null, jailCards = emptyList(), bankrupt = true)
    after = after.copy(players = players)
    if (over) return Result(gameOver(config, after, standing, events), events)
    if (debt.creditor == null && owned.isNotEmpty()) {
        after = startAuction(config, after, owned.first(), events, owned.drop(1), then = resume)
        resume = after.phase
    }
    val rest = phase.debts.drop(1)
    return Result(after.copy(phase = if (rest.isEmpty()) resume else TurnPhase.Debt(rest, resume)), events)
}

/**
 * R-34: todo al acreedor. Los edificios vuelven al Banco a la mitad y ese dinero es del acreedor;
 * por cada hipotecada paga ya `bankruptcyInterest` y puede levantarla este turno sin más interés
 * (`feePaid`). El saldo negativo se descuenta al acreedor hasta lo que cobró en la acción; el resto
 * lo pone el Banco (D-15). `whole` (R-39): los edificios pasan enteros y no hay interés.
 */
private fun toPlayer(config: GameConfig, state: GameState, debt: PlayerDebt, owned: List<Int>, whole: Boolean): GameState {
    val creditor = debt.creditor!!
    val rules = config.rules
    var after = state
    var cash = maxOf(state.players[debt.debtor].money, -debt.amount)
    for (square in owned) {
        val holding = after.holdings.getValue(square)
        var given = holding.copy(owner = creditor, feePaid = false)
        if (!whole) {
            val property = config.squares[square] as? Property
            if (property != null) cash += buildingsRefund(config, property, holding)
            after = returnBuildings(config, after, holding)
            given = given.copy(houses = 0, hotel = false)
        }
        after = after.copy(holdings = after.holdings + (square to given))
        if (holding.mortgaged && !whole) {
            cash -= percentOf(mortgageValue(config, after, square), rules.bankruptcyInterest, rules.percentRounding)
            after = after.copy(holdings = after.holdings + (square to given.copy(feePaid = true)))
        }
    }
    val players = after.players.toMutableList()
    players[creditor] = players[creditor].copy(jailCards = players[creditor].jailCards + players[debt.debtor].jailCards)
    return after.copy(players = players).addMoney(creditor, cash)
}

/** R-35: los edificios vuelven al Banco sin pagarse, las casillas quedan libres y las cartas, debajo de su mazo. */
private fun toBank(config: GameConfig, state: GameState, player: Int, owned: List<Int>): GameState {
    var after = state
    for (square in owned) after = returnBuildings(config, after, after.holdings.getValue(square))
    after = after.copy(holdings = after.holdings - owned.toSet())
    for (card in state.players[player].jailCards) after = after.putUnder(config, card)
    return after
}

/** Las casas y el hotel de `holding` vuelven a las existencias del Banco, si son limitadas (R-28). */
private fun returnBuildings(config: GameConfig, state: GameState, holding: Holding): GameState {
    val rules = config.rules
    val houses = if (rules.houseStock > 0) holding.houses else 0
    val hotels = if (rules.hotelStock > 0 && holding.hotel) 1 else 0
    return state.copy(bankHouses = state.bankHouses + houses, bankHotels = state.bankHotels + hotels)
}

/**
 * Lo que da vender al Banco los edificios de un solar, de uno en uno a la mitad (R-30); el
 * hotel, con sus `maxHouses` casas si `hotelReturnsHouses`.
 */
internal fun buildingsRefund(config: GameConfig, property: Property, holding: Holding): Int {
    val rules = config.rules
    val half = { amount: Int -> percentOf(amount, 50, rules.percentRounding) }
    val house = half(rules.housePrice ?: property.housePrice ?: 0)
    var total = holding.houses * house
    if (holding.hotel) {
        total += half(rules.hotelPrice ?: property.hotelPrice ?: 0)
        if (rules.hotelReturnsHouses) total += rules.maxHouses * house
    }
    return total
}

/**
 * Lo más que `player` puede juntar vendiendo todos sus edificios (si `sellBuildingsToBank`) e
 * hipotecando lo que no lo está (R-30, R-31, R-49). Sin vender y sin `mortgageWithBuildings`,
 * un grupo con edificios no se hipoteca.
 */
internal fun raisable(config: GameConfig, state: GameState, player: Int): Int {
    val rules = config.rules
    return state.holdings.filterValues { it.owner == player && !it.mortgaged }.keys.sumOf { square ->
        val holding = state.holdings.getValue(square)
        val sq = config.squares[square] as OwnableSquare
        val bare = state.copy(holdings = state.holdings + (square to holding.copy(houses = 0, hotel = false)))
        val built = sq is Property && groupSquares(config, sq.group).any { state.holdings[it].let { h -> h != null && (h.houses > 0 || h.hotel) } }
        val canMortgage = rules.mortgageValue == MortgageValue.HALF_TOTAL || sq.mortgage != null
        when {
            !canMortgage -> if (rules.sellBuildingsToBank && sq is Property) buildingsRefund(config, sq, holding) else 0
            rules.sellBuildingsToBank && sq is Property -> buildingsRefund(config, sq, holding) + mortgageValue(config, bare, square)
            rules.mortgageWithBuildings -> mortgageValue(config, state, square)
            built -> 0
            else -> mortgageValue(config, state, square)
        }
    }
}

/**
 * Recuento de R-39: efectivo + casillas a su precio impreso (las hipotecadas, a la mitad) + casas
 * y hoteles a su precio de compra, el hotel con las casas que se entregaron (`buildingsCost`).
 */
fun wealth(config: GameConfig, state: GameState, player: Int): Int {
    val rules = config.rules
    return state.players[player].money + state.holdings.filterValues { it.owner == player }.entries.sumOf { (square, holding) ->
        val sq = config.squares[square] as OwnableSquare
        val price = if (holding.mortgaged) percentOf(sq.price, 50, rules.percentRounding) else sq.price
        price + if (sq is Property) buildingsCost(config, sq, holding) else 0
    }
}

/** Se acabó el tiempo del juego con límite: recuento entre los que siguen (R-40, D-15). */
internal fun timeUp(config: GameConfig, state: GameState): Result {
    if (config.rules.endCondition != EndCondition.TIME_LIMIT) throw IllegalActionException("la partida no tiene límite de tiempo (R-40)")
    val events = mutableListOf<Event>()
    val standing = state.players.indices.filter { !state.players[it].bankrupt }
    return Result(gameOver(config, state, standing, events), events)
}

/** Fin: gana el más rico de `standing` (R-39, R-40); empate → el de más efectivo; si sigue, todos (D-15). */
private fun gameOver(config: GameConfig, state: GameState, standing: List<Int>, events: MutableList<Event>): GameState {
    val richest = standing.maxOf { wealth(config, state, it) }
    val tied = standing.filter { wealth(config, state, it) == richest }
    val cash = tied.maxOf { state.players[it].money }
    val winners = tied.filter { state.players[it].money == cash }
    events += Event.GameOver(winners)
    return state.copy(phase = TurnPhase.Over(winners))
}

/**
 * Juego corto o con tiempo (R-37, R-40): se barajan las Escrituras con la semilla y se reparten
 * `startingDeeds` a cada uno, de una en una desde quien empieza; cada uno paga su precio impreso.
 */
internal fun dealDeeds(config: GameConfig, state: GameState, events: MutableList<Event>): GameState {
    val perPlayer = config.rules.startingDeeds
    if (perPlayer == 0) return state
    val (deeds, random) = state.random.shuffled(config.squares.indices.filter { config.squares[it] is OwnableSquare })
    val count = state.players.size
    var after = state.copy(random = random)
    for ((i, square) in deeds.take(perPlayer * count).withIndex()) {
        val player = (state.current + i) % count
        val price = (config.squares[square] as OwnableSquare).price
        events += Event.DeedDealt(player, square, price)
        after = after.addMoney(player, -price).copy(holdings = after.holdings + (square to Holding(player)))
    }
    return after
}
