package com.jacck.mono.game

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase

/** La línea de arriba del letrero (D-69): de quién es la decisión y sobre qué. */
sealed interface BarTitle {
    /** «Te toca, Andrés». */
    data class Turn(val player: Int) : BarTitle
    /** «¿Compras Calle 19?». */
    data class Buy(val square: Int) : BarTitle
    /** «Subasta de Calle 19: puja Andrés». */
    data class Auction(val square: Int, val bidder: Int) : BarTitle
    /** «Impuesto sobre la renta: ¿cómo pagas?». */
    data class Tax(val square: Int) : BarTitle
    /** «Andrés en la Cárcel (1 de 3)». */
    data class Jail(val player: Int, val turn: Int, val of: Int) : BarTitle
    /** «Andrés tiene −$40 y debe pagar». */
    data class Debt(val player: Int) : BarTitle
    /** «Juega Botty 2»: la máquina piensa. */
    data class Machine(val player: Int) : BarTitle
    /** «Botty 2 jugó»: la máquina espera «Seguir». */
    data class MachinePlayed(val player: Int) : BarTitle
    /** «Esperando a Beto»: decide el otro teléfono. */
    data class Waiting(val player: Int) : BarTitle
}

/** Qué dice un botón; la cifra va en [BarButton.amount]. */
enum class BarKind {
    ROLL, END_TURN, BUY, DECLINE, BID, PASS, TAX_FIXED, TAX_PERCENT, JAIL_ROLL, JAIL_FINE, JAIL_CARD,
    /** Abre la hoja de propiedades del deudor con sus ventas e hipotecas. */
    SELL_OR_MORTGAGE,
    /** Abre el «¿Seguro?» de la quiebra (D-56): es del teléfono, va en ventana (D-67). */
    BANKRUPTCY,
    /** Deja jugar a la máquina. */
    NEXT,
}

/**
 * Un botón del letrero: [action] es la del motor (null si lo resuelve la pantalla), [principal] rojo
 * u ocre, y [enabled] si el motor la acepta ahora (`Engine.tryApply`, D-24).
 */
data class BarButton(
    val kind: BarKind, val action: Action? = null, val amount: Int? = null, val principal: Boolean = true,
    val enabled: Boolean = true,
)

data class TurnBar(val title: BarTitle, val buttons: List<BarButton>)

/**
 * El letrero del turno (FD.4, D-67, D-69, D-70): título y botones sacados de la fase del motor.
 * Si [held] (la máquina que acaba de jugar), «Seguir»; si decide uno de [bots], solo el título; si
 * decide uno de [remote] (otro teléfono), «Esperando a». Null con la partida terminada.
 */
fun turnBar(config: GameConfig, state: GameState, bots: Set<Int> = emptySet(), remote: Set<Int> = emptySet(), held: Int? = null): TurnBar? {
    val phase = state.phase
    if (phase is TurnPhase.Over) return null
    if (held != null) return TurnBar(BarTitle.MachinePlayed(held), listOf(BarButton(BarKind.NEXT)))
    val who = decider(state)
    if (who in remote) return TurnBar(BarTitle.Waiting(who), emptyList())
    if (who in bots) return TurnBar(BarTitle.Machine(who), emptyList())
    fun button(kind: BarKind, action: Action, amount: Int? = null, principal: Boolean = true) =
        BarButton(kind, action, amount, principal, Engine.tryApply(config, state, action) != null)
    return when (phase) {
        TurnPhase.Roll -> {
            val player = state.players[who]
            val turns = player.jailTurns
            if (turns == null) {
                TurnBar(BarTitle.Turn(who), listOf(button(BarKind.ROLL, Action.Roll)))
            } else {
                val fine = button(BarKind.JAIL_FINE, Action.PayJailFine, config.rules.jailFine, principal = false)
                val card = button(BarKind.JAIL_CARD, Action.UseJailCard, principal = false)
                // Lo que seguro no vale (sin carta, sin dinero, último turno) no se ofrece (R-22).
                TurnBar(
                    BarTitle.Jail(who, turns + 1, config.rules.jailMaxTurns),
                    listOf(button(BarKind.JAIL_ROLL, Action.Roll)) + listOf(fine, card).filter { it.enabled },
                )
            }
        }
        TurnPhase.EndOfTurn -> TurnBar(BarTitle.Turn(who), listOf(button(BarKind.END_TURN, Action.EndTurn)))
        is TurnPhase.Buy -> TurnBar(
            BarTitle.Buy(phase.square),
            listOf(
                button(BarKind.BUY, Action.Buy, (config.squares[phase.square] as OwnableSquare).price),
                button(BarKind.DECLINE, Action.Decline, principal = false),
            ),
        )
        is TurnPhase.Auction -> {
            val base = minimumBid(config, phase.square)
            val offers = if (phase.highestBidder == null) listOf(base, base + 10, base + 50) else listOf(10, 50, 100).map { phase.highestBid + it }
            TurnBar(
                BarTitle.Auction(phase.square, who),
                offers.map { button(BarKind.BID, Action.Bid(who, it), it) } + button(BarKind.PASS, Action.PassBid(who), principal = false),
            )
        }
        is TurnPhase.TaxChoice -> {
            val tax = config.squares[phase.square] as Tax
            TurnBar(
                BarTitle.Tax(phase.square),
                listOf(
                    button(BarKind.TAX_FIXED, Action.PayTax(percent = false), tax.fixed),
                    button(BarKind.TAX_PERCENT, Action.PayTax(percent = true), tax.percent, principal = false),
                ),
            )
        }
        is TurnPhase.Debt -> {
            val moves = state.holdings.filterValues { it.owner == who }.keys.any { propertyMoves(config, state, it).isNotEmpty() }
            TurnBar(
                BarTitle.Debt(who),
                listOf(
                    BarButton(BarKind.SELL_OR_MORTGAGE, enabled = moves),
                    BarButton(BarKind.BANKRUPTCY, principal = !moves),
                ),
            )
        }
        is TurnPhase.Over -> null
    }
}
