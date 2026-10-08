package com.jacck.mono.demo

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.rollDice
import com.jacck.mono.engine.model.CardEffect
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.PlayerDebt
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase

/** Valores del extra `fase` (FB.2): cada uno abre la partida en el diálogo de turno que nombra. */
val SAMPLE_PHASES = listOf("compra", "subasta", "carcel", "impuesto", "deuda", "fin", "alquiler", "carta")

/**
 * Deja la partida recién repartida en el diálogo de [phase] para capturarlo sin jugar (FB.2; adb no
 * puede tocar la pantalla, D-23). Quien empieza cae en la primera propiedad (compra); la rechaza y
 * puja el mínimo, así la subasta ya tiene quien gana (por el motor); entra a la Cárcel; cae en el
 * impuesto con porcentaje, o el primero; queda con −$50 y las propiedades de muestra (deuda); o gana.
 * Con `alquiler` y `carta` (FD.2, D-66) queda a la distancia de los dados que van a salir de una
 * propiedad de otro (las de muestra) o de una casilla de carta con «cobra a cada jugador» encima del
 * mazo: al tirar, paga el alquiler o cobra la carta.
 */
fun withPhase(config: GameConfig, phase: String): (GameState) -> GameState = { state ->
    val me = state.current
    val property = config.squares.indexOfFirst { it is Property }
    val buying = state.copy(
        players = state.players.mapIndexed { i, p -> if (i == me) p.copy(position = property) else p },
        phase = TurnPhase.Buy(property),
    )
    when (phase) {
        "compra" -> buying
        "subasta" -> {
            val auction = Engine.apply(config, buying, Action.Decline).state
            Engine.apply(config, auction, Action.Bid(me, minimumBid(config, property))).state
        }
        "carcel" -> state.copy(
            players = state.players.mapIndexed { i, p ->
                if (i == me) p.copy(position = config.squares.indexOfFirst { it is Jail }, jailTurns = 0) else p
            },
        )
        "impuesto" -> {
            val tax = config.squares.indexOfFirst { it is Tax && it.percent > 0 }
                .takeIf { it >= 0 } ?: config.squares.indexOfFirst { it is Tax }
            state.copy(
                players = state.players.mapIndexed { i, p -> if (i == me) p.copy(position = tax) else p },
                phase = TurnPhase.TaxChoice(tax),
            )
        }
        "deuda" -> withSampleProperties(state).let { s ->
            s.copy(
                players = s.players.mapIndexed { i, p -> if (i == me) p.copy(money = -50) else p },
                phase = TurnPhase.Debt(listOf(PlayerDebt(me, null)), TurnPhase.EndOfTurn),
            )
        }
        "fin" -> state.copy(phase = TurnPhase.Over(listOf(me)))
        "alquiler" -> withSampleProperties(state).let { s -> s.landingOn(config, s.holdings.filterValues { it.owner != me }.keys.max()) }
        "carta" -> {
            val card = config.cards.indexOfFirst { it.effect is CardEffect.CollectFromEach }
            val deck = config.cards[card].deck
            val square = config.squares.indexOfFirst { it is CardSquare && it.deck == deck }
            state.copy(decks = state.decks + (deck to listOf(card) + state.decks[deck].orEmpty().minus(card))).landingOn(config, square)
        }
        else -> state
    }
}

/** Quien juega queda a la distancia de los próximos dados (`random` es puro) de [target], así cae ahí al tirar. */
private fun GameState.landingOn(config: GameConfig, target: Int): GameState {
    val at = Math.floorMod(target - random.rollDice().first.total, config.squares.size)
    return copy(players = players.mapIndexed { i, p -> if (i == current) p.copy(position = at) else p })
}
