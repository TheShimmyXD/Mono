package com.jacck.mono.demo

import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Cada valor del extra `fase` deja la partida en el diálogo que nombra, en los dos presets (FB.2). */
class SamplePhasesTest {

    @Test
    fun cadaFaseAbreSuDialogo() {
        for (preset in listOf(Preset.CLASSIC, Preset.TIO_RICO)) {
            val config = preset.load()
            val start = Engine.newGame(config, listOf("Ana", "Beto", "Caro"), 7).state
            val phases = SAMPLE_PHASES.associateWith { withPhase(config, it)(start) }
            assertTrue(phases.getValue("compra").phase is TurnPhase.Buy, "$preset compra")
            val auction = phases.getValue("subasta").phase as TurnPhase.Auction
            assertEquals(start.current, auction.highestBidder, "$preset subasta")
            assertEquals(0, phases.getValue("carcel").players[start.current].jailTurns, "$preset cárcel")
            assertTrue(phases.getValue("impuesto").phase is TurnPhase.TaxChoice, "$preset impuesto")
            assertTrue(phases.getValue("deuda").phase is TurnPhase.Debt, "$preset deuda")
            assertEquals(TurnPhase.Over(listOf(start.current)), phases.getValue("fin").phase, "$preset fin")
            // Al tirar, quien juega paga un alquiler o cobra a cada uno con una carta (FD.2, D-66).
            val rent = Engine.apply(config, phases.getValue("alquiler"), Action.Roll).events
            assertTrue(rent.any { it is Event.RentPaid && it.payer == start.current }, "$preset alquiler: $rent")
            val card = Engine.apply(config, phases.getValue("carta"), Action.Roll).events
            assertEquals(2, card.count { it is Event.CardPayment && it.to == start.current && it.from != null }, "$preset carta: $card")
        }
    }
}
