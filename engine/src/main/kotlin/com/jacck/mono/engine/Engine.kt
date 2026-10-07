package com.jacck.mono.engine

import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.PlayerState
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.StartTieRule
import com.jacck.mono.engine.model.TurnPhase

/** Estado nuevo y lo que pasó para llegar a él. */
data class Result(val state: GameState, val events: List<Event>)

/** Una acción que no vale en la fase actual de la partida. */
class IllegalActionException(message: String) : IllegalStateException(message)

/**
 * El motor (D-02, D-03): funciones puras de (configuración, estado, acción) a estado nuevo.
 * Todo el azar sale de `GameState.random`; con la misma semilla y las mismas acciones la
 * partida es la misma.
 */
object Engine {

    /**
     * Partida nueva: todos en la salida con el dinero inicial (R-07, R-03, R-42), empieza
     * el de la tirada inicial mayor (R-06, R-43), los mazos se barajan (R-18, D-14) y, en el
     * juego corto o con tiempo, se reparten Escrituras (R-37, R-40). `tokens` = personaje de cada
     * jugador, en el orden de `names` (FB.4, D-36); si falta, null.
     */
    fun newGame(config: GameConfig, names: List<String>, seed: Long, tokens: List<String> = emptyList()): Result {
        val dice = SeededDice(GameRandom(seed))
        val (first, rolled) = firstPlayer(names.size, config.rules.startTieRule, dice)
        val events = rolled.toMutableList()
        val start = startIndex(config)
        val (decks, random) = shuffleDecks(config, dice.random)
        val state = GameState(
            players = names.mapIndexed { i, name -> PlayerState(name, config.rules.startingMoney, start, token = tokens.getOrNull(i)) },
            holdings = emptyMap(),
            current = first,
            phase = TurnPhase.Roll,
            doublesInRow = 0,
            bankHouses = config.rules.houseStock,
            bankHotels = config.rules.hotelStock,
            pot = 0,
            turn = 0,
            random = random,
            decks = decks,
        )
        return Result(dealDeeds(config, state, events), events)
    }

    /**
     * Quién empieza: cada uno tira y empieza el total mayor (R-06, R-43). Si hay empate en
     * el mayor, repiten los empatados o todos según `rule` (D-08).
     */
    fun firstPlayer(count: Int, rule: StartTieRule, dice: Iterator<Dice>): Pair<Int, List<Event>> {
        require(count >= 1) { "sin jugadores" }
        val events = mutableListOf<Event>()
        var contenders = (0 until count).toList()
        while (true) {
            val rolls = contenders.associateWith { dice.next() }
            events += Event.StartRolled(rolls)
            val best = rolls.values.maxOf { it.total }
            val tied = contenders.filter { rolls.getValue(it).total == best }
            if (tied.size == 1) {
                events += Event.FirstPlayer(tied.single())
                return tied.single() to events
            }
            if (rule == StartTieRule.REROLL_TIED) contenders = tied
        }
    }

    /**
     * Aplica una acción y, si alguien quedó con saldo negativo, abre la deuda (R-34, R-35).
     * Los negocios entre jugadores (`Trade`) aún no están.
     */
    fun apply(config: GameConfig, state: GameState, action: Action): Result {
        if (state.phase is TurnPhase.Over) throw IllegalActionException("la partida terminó")
        val (after, events) = dispatch(config, state, action)
        val all = events.toMutableList()
        return Result(openDebts(after, all), all)
    }

    /**
     * Prueba una acción sin tocar `state`: su resultado si el motor la acepta, o null si no. La
     * interfaz lo usa para saber qué botones mostrar y con qué cifra (F3.4), sin repetir reglas.
     */
    fun tryApply(config: GameConfig, state: GameState, action: Action): Result? =
        try {
            apply(config, state, action)
        } catch (e: IllegalActionException) {
            null
        }

    private fun dispatch(config: GameConfig, state: GameState, action: Action): Result = when (action) {
        Action.Roll -> {
            val (dice, next) = state.random.rollDice()
            move(config, state.copy(random = next), dice)
        }
        Action.Buy -> buy(config, state)
        Action.Decline -> decline(config, state)
        is Action.Bid -> bid(config, state, action.player, action.amount)
        is Action.PassBid -> passBid(config, state, action.player)
        is Action.Build -> build(config, state, action.square)
        is Action.SellBuilding -> sellBuilding(config, state, action.square)
        is Action.Mortgage -> mortgage(config, state, action.square)
        is Action.Unmortgage -> unmortgage(config, state, action.square)
        is Action.PayTax -> payTax(config, state, action.percent)
        Action.PayJailFine -> payJailFine(config, state)
        Action.UseJailCard -> useJailCard(config, state)
        Action.DeclareBankruptcy -> declareBankruptcy(config, state)
        Action.TimeUp -> timeUp(config, state)
        Action.EndTurn -> endTurn(state)
        is Action.Trade -> throw UnsupportedOperationException("acción aún no implementada: $action")
    }

    /**
     * Tirada con dados conocidos: avanza la suma (R-07), cobra el sueldo por cada paso por
     * la salida (R-10, R-45), resuelve la casilla (R-08) y, con dobles, vuelve a tirar (R-09, R-43).
     * Con `doublesToJail` dobles seguidos no avanza: va a la Cárcel (R-09, R-20). Desde la
     * Cárcel tira para salir (R-22). Si alguien queda con saldo negativo, abre la deuda (R-34).
     */
    fun roll(config: GameConfig, state: GameState, dice: Dice): Result {
        val (after, events) = move(config, state, dice)
        val all = events.toMutableList()
        return Result(openDebts(after, all), all)
    }

    private fun move(config: GameConfig, state: GameState, dice: Dice): Result {
        if (state.phase != TurnPhase.Roll) throw IllegalActionException("no toca tirar: ${state.phase}")
        val player = state.current
        if (state.players[player].jailTurns != null) return rollInJail(config, state, dice)
        val events = mutableListOf<Event>(Event.DiceRolled(player, dice))
        val doubles = if (dice.isDouble) state.doublesInRow + 1 else 0
        val limit = config.rules.doublesToJail
        if (limit > 0 && doubles >= limit) {
            return Result(sendToJail(config, state, player, JailCause.DOUBLES, events), events)
        }
        val moved = advance(config, state, player, dice.total, events).copy(doublesInRow = doubles)
        val landed = land(config, moved, player, dice, events)
        return Result(if (landed.phase == TurnPhase.Roll) finishMove(config, landed, events) else landed, events)
    }

    /** Termina el turno (R-06). */
    private fun endTurn(state: GameState): Result {
        if (state.phase != TurnPhase.EndOfTurn) throw IllegalActionException("no se puede terminar: ${state.phase}")
        val events = mutableListOf<Event>()
        return Result(passTurn(state, events), events)
    }

    /**
     * Pasa el turno al siguiente de la lista que no esté en quiebra (R-06). El interés ya pagado
     * de una hipotecada recibida vale solo hasta aquí (R-34).
     */
    internal fun passTurn(state: GameState, events: MutableList<Event>): GameState {
        val count = state.players.size
        val next = (1..count).map { (state.current + it) % count }.first { !state.players[it].bankrupt }
        events += Event.TurnPassed(next)
        val holdings = state.holdings.mapValues { (_, h) -> if (h.feePaid) h.copy(feePaid = false) else h }
        return state.copy(current = next, phase = TurnPhase.Roll, doublesInRow = 0, turn = state.turn + 1, holdings = holdings)
    }

    /**
     * Mueve `steps` casillas hacia adelante en el anillo (R-07) y paga un sueldo por cada vez
     * que cae en la salida o pasa por ella (R-10: una vez por vuelta; R-45).
     */
    internal fun advance(
        config: GameConfig,
        state: GameState,
        player: Int,
        steps: Int,
        events: MutableList<Event>,
    ): GameState {
        val size = config.squares.size
        val start = startIndex(config)
        val from = state.players[player].position
        val to = (from + steps) % size
        val laps = ((from - start + size) % size + steps) / size
        events += Event.Moved(player, from, to)
        val salary = laps * config.rules.salary
        if (laps > 0) repeat(laps) { events += Event.SalaryPaid(player, config.rules.salary) }
        val players = state.players.toMutableList()
        players[player] = players[player].copy(position = to)
        return state.copy(players = players).addMoney(player, salary)
    }

    /** Casilla resuelta: con dobles vuelve a tirar (R-09, R-43); si no, fin del turno. */
    internal fun finishMove(config: GameConfig, state: GameState, events: MutableList<Event>): GameState {
        val again = state.doublesInRow > 0 && config.rules.doublesRollAgain
        if (again) events += Event.RollAgain(state.current)
        return state.copy(phase = if (again) TurnPhase.Roll else TurnPhase.EndOfTurn)
    }

    /** Índice de la salida en el anillo (R-07). */
    private fun startIndex(config: GameConfig): Int =
        config.squares.indexOfFirst { it is Start }.also { require(it >= 0) { "el tablero no tiene salida" } }
}

/** Suma `delta` (o resta, si es negativo) al dinero de `player`. */
internal fun GameState.addMoney(player: Int, delta: Int): GameState {
    if (delta == 0) return this
    val list = players.toMutableList()
    list[player] = list[player].copy(money = list[player].money + delta)
    return copy(players = list)
}
