package com.jacck.mono.engine.bot

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.TurnPhase
import kotlin.random.Random

/**
 * Jugador automático (F5.8, D-54): la acción que toca a [seat] en [state], o null si ahora no le toca.
 * Solo elige entre jugadas que el motor acepta (`Engine.tryApply`): las reglas son del motor, no suyas.
 * Su azar es [random], con semilla, no el de la partida (D-03).
 */
fun interface Bot {
    fun choose(config: GameConfig, state: GameState, seat: Int, random: Random): Action?
}

/** Si a [seat] le toca hacer algo: pujar (cualquiera que siga en la subasta), pagar una deuda o su turno. */
private fun turnOf(state: GameState, seat: Int): Boolean = when (val phase = state.phase) {
    is TurnPhase.Over -> false
    is TurnPhase.Auction -> seat in phase.bidders
    is TurnPhase.Debt -> phase.debts.first().debtor == seat
    else -> state.current == seat
}

private fun owned(state: GameState, seat: Int) = state.holdings.filterValues { it.owner == seat }.keys.toList()

/**
 * La máquina simple (la de `mono-pc`, D-50): compra siempre que puede y a veces construye, para que la
 * partida termine; en la subasta pasa; en una deuda vende casas, hipoteca y al final quiebra.
 */
object SimpleBot : Bot {
    override fun choose(config: GameConfig, state: GameState, seat: Int, random: Random): Action? {
        if (!turnOf(state, seat)) return null
        val options = when (state.phase) {
            is TurnPhase.Auction -> listOf(Action.PassBid(seat))
            is TurnPhase.Debt -> owned(state, seat).map { Action.SellBuilding(it) } + owned(state, seat).map { Action.Mortgage(it) } +
                Action.DeclareBankruptcy
            TurnPhase.Roll -> listOf(Action.UseJailCard, Action.Roll)
            is TurnPhase.Buy -> listOf(Action.Buy, Action.Decline)
            is TurnPhase.TaxChoice -> listOf(Action.PayTax(percent = false), Action.PayTax(percent = true))
            TurnPhase.EndOfTurn ->
                (if (random.nextInt(3) == 0) owned(state, seat).shuffled(random).map { Action.Build(it) } else emptyList()) + Action.EndTurn
            else -> emptyList()
        }
        return options.firstOrNull { Engine.tryApply(config, state, it) != null }
    }
}

/**
 * La máquina lista (F5.8, D-54): guarda una reserva de [RESERVE] para los alquileres; compra si le queda
 * la reserva o si completa un grupo; en la subasta puja hasta el precio (×1,5 si completa o bloquea un
 * grupo); construye parejo y sin bajar de la reserva; levanta hipotecas con el doble de reserva; en el
 * impuesto paga lo menos; en una deuda hipoteca primero lo que no es de un grupo completo y vende casas
 * solo al final. No usa [random]: con el mismo estado juega igual.
 */
object SmartBot : Bot {
    const val RESERVE = 50

    override fun choose(config: GameConfig, state: GameState, seat: Int, random: Random): Action? {
        if (!turnOf(state, seat)) return null
        fun after(action: Action) = Engine.tryApply(config, state, action)?.state?.players?.get(seat)?.money
        fun first(vararg actions: Action) = actions.firstOrNull { after(it) != null }
        val money = state.players[seat].money
        return when (val phase = state.phase) {
            is TurnPhase.Auction -> auction(config, state, seat, phase)
            is TurnPhase.Debt -> debt(config, state, seat)
            TurnPhase.Roll -> first(Action.UseJailCard, Action.Roll)
            is TurnPhase.Buy -> {
                val price = (config.squares[phase.square] as OwnableSquare).price
                val wanted = money - price >= RESERVE || completes(config, state, seat, phase.square)
                if (wanted) first(Action.Buy, Action.Decline) else first(Action.Decline)
            }
            is TurnPhase.TaxChoice ->
                listOf(Action.PayTax(percent = false), Action.PayTax(percent = true)).filter { after(it) != null }.maxByOrNull { after(it)!! }
            TurnPhase.EndOfTurn -> build(config, state, seat) ?: unmortgage(config, state, seat) ?: Action.EndTurn
            else -> null
        }
    }

    /** Pujar uno más que la mayor (o la base) mientras no pase de lo que vale para ella; si no, retirarse. */
    private fun auction(config: GameConfig, state: GameState, seat: Int, phase: TurnPhase.Auction): Action? {
        if (phase.highestBidder == seat) return null // va ganando: espera a los demás
        val price = (config.squares[phase.square] as OwnableSquare).price
        val worth = if (completes(config, state, seat, phase.square) || blocks(config, state, seat, phase.square)) price * 3 / 2 else price
        val limit = minOf(worth, state.players[seat].money - RESERVE / 2)
        val next = maxOf(phase.highestBid + maxOf(1, price / 20), minimumBid(config, phase.square))
        val bid = Action.Bid(seat, next)
        return if (next <= limit && Engine.tryApply(config, state, bid) != null) bid else Action.PassBid(seat)
    }

    /** Hipotecar lo suelto, vender casas, hipotecar lo de grupos completos y, si nada alcanza, quebrar. */
    private fun debt(config: GameConfig, state: GameState, seat: Int): Action {
        val mine = owned(state, seat)
        val (grouped, loose) = mine.partition { ownsGroup(config, state, seat, it) }
        val options = loose.sortedBy { (config.squares[it] as OwnableSquare).price }.map { Action.Mortgage(it) } +
            mine.sortedByDescending { state.holdings.getValue(it).let { h -> if (h.hotel) 99 else h.houses } }.map { Action.SellBuilding(it) } +
            grouped.map { Action.Mortgage(it) }
        return options.firstOrNull { Engine.tryApply(config, state, it) != null } ?: Action.DeclareBankruptcy
    }

    /** Una casa donde haya menos (parejo), la más barata primero, si después le queda la reserva. */
    private fun build(config: GameConfig, state: GameState, seat: Int): Action? =
        owned(state, seat).filter { config.squares[it] is Property }
            .sortedWith(compareBy({ state.holdings.getValue(it).houses }, { (config.squares[it] as Property).price }))
            .map { Action.Build(it) }
            .firstOrNull { (Engine.tryApply(config, state, it)?.state?.players?.get(seat)?.money ?: -1) >= RESERVE }

    /** Levantar una hipoteca si después le queda el doble de la reserva; primero las de grupos completos. */
    private fun unmortgage(config: GameConfig, state: GameState, seat: Int): Action? =
        owned(state, seat).filter { state.holdings.getValue(it).mortgaged }
            .sortedByDescending { ownsGroup(config, state, seat, it) }
            .map { Action.Unmortgage(it) }
            .firstOrNull { (Engine.tryApply(config, state, it)?.state?.players?.get(seat)?.money ?: -1) >= 2 * RESERVE }

    /** Casillas del grupo de color de [square] (vacío si no es una propiedad). */
    private fun group(config: GameConfig, square: Int): List<Int> {
        val p = config.squares[square] as? Property ?: return emptyList()
        return config.squares.indices.filter { (config.squares[it] as? Property)?.group == p.group }
    }

    private fun ownsGroup(config: GameConfig, state: GameState, seat: Int, square: Int): Boolean =
        group(config, square).let { g -> g.isNotEmpty() && g.all { state.holdings[it]?.owner == seat } }

    /** Con [square], [seat] tendría el grupo entero. */
    private fun completes(config: GameConfig, state: GameState, seat: Int, square: Int): Boolean =
        group(config, square).let { g -> g.isNotEmpty() && g.all { it == square || state.holdings[it]?.owner == seat } }

    /** Las demás casillas del grupo son todas de un mismo rival: si la gana él, completa el grupo. */
    private fun blocks(config: GameConfig, state: GameState, seat: Int, square: Int): Boolean {
        val others = group(config, square).filter { it != square }.map { state.holdings[it]?.owner }
        return others.isNotEmpty() && others.all { it != null && it != seat && it == others.first() }
    }
}
