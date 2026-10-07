package com.jacck.mono.demo

import com.jacck.mono.engine.Engine
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
        }
    }
}
