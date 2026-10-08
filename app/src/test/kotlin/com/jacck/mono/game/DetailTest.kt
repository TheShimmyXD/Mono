package com.jacck.mono.game

import com.jacck.mono.engine.model.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Lo que muestra la ventana y el deslizar hacia arriba para volver (FD.6, D-72). */
class DetailTest {

    @Test
    fun `FD-6 sin nada tocado, lo que pasa y al final, el final`() {
        assertEquals(Shown.Log, shown(null, TurnPhase.Roll))
        assertEquals(Shown.Over, shown(null, TurnPhase.Over(listOf(1))))
    }

    @Test
    fun `FD-6 lo tocado va antes que lo que pasa y que el final`() {
        assertEquals(Shown.Square(3), shown(Shown.Square(3), TurnPhase.Roll))
        assertEquals(Shown.Player(2), shown(Shown.Player(2), TurnPhase.Over(listOf(1))))
    }

    @Test
    fun `FD-6 tocar lo mismo lo cierra y tocar otra cosa la reemplaza`() {
        assertNull(tapped(Shown.Player(1), Shown.Player(1)))
        assertEquals(Shown.Square(5), tapped(Shown.Player(1), Shown.Square(5)))
        assertEquals(Shown.Player(0), tapped(null, Shown.Player(0)))
    }

    @Test
    fun `FD-6 sale volando si subio lo bastante o si se lanzo rapido hacia arriba`() {
        assertTrue(swipeCloses(-SWIPE_DP, 0f))
        assertTrue(swipeCloses(-10f, -SWIPE_SPEED_DP))
        assertFalse(swipeCloses(-SWIPE_DP + 1, -SWIPE_SPEED_DP + 1))
        assertFalse(swipeCloses(0f, 3000f))
    }
}
