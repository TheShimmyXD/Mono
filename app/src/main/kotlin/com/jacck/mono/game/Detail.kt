package com.jacck.mono.game

import com.jacck.mono.engine.model.TurnPhase

/** Lo que muestra la ventana bajo el letrero (FD.6, D-72): lo que pasa, el final, una casilla o un jugador. */
sealed interface Shown {
    data object Log : Shown
    data object Over : Shown
    data class Square(val square: Int) : Shown
    data class Player(val player: Int) : Shown
}

/** Lo tocado va primero; sin nada tocado, el final de la partida o lo que pasa. */
fun shown(detail: Shown?, phase: TurnPhase): Shown = detail ?: if (phase is TurnPhase.Over) Shown.Over else Shown.Log

/** Tocar lo que ya se ve lo cierra; tocar otra cosa la pone en su lugar. */
fun tapped(detail: Shown?, touched: Shown): Shown? = if (detail == touched) null else touched

/** Distancia (dp) que hay que subir la ventana para que salga volando al soltar. */
const val SWIPE_DP = 80f

/** Velocidad hacia arriba (dp/s) con la que sale aunque no haya subido [SWIPE_DP]. */
const val SWIPE_SPEED_DP = 1200f

/** Al soltar: [lift] cuánto subió (negativo, hacia arriba) y [speed] su velocidad (negativa, hacia arriba), en dp. */
fun swipeCloses(lift: Float, speed: Float): Boolean = lift <= -SWIPE_DP || speed <= -SWIPE_SPEED_DP

/** Lo que tarda en salir volando la ventana al soltarla (ms). */
const val SWIPE_MS = 250
