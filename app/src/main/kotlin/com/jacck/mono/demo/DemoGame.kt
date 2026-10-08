package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.board.Board
import com.jacck.mono.Calcomania
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.PlayersRow
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Start

/**
 * Partida de muestra para ver el tablero (F3.2) hasta que existan la partida de verdad (F3.3) y el
 * editor (F4): el Clásico (40), el Tío Rico (44) o el Clásico recortado o alargado a [n] casillas,
 * con dueños, casas y fichas repartidos a mano. No se juega: solo se dibuja.
 */
@Composable
fun DemoBoardScreen(n: Int, players: Int) {
    val config = remember(n) { demoConfig(n) }
    val state = remember(config, players) { demoState(config, players) }
    PantallaChiva {
        Board(config, state, Modifier.fillMaxSize().padding(2.dp)) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
                Text(
                    "${config.name} · ${config.squares.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Calcomania { PlayersRow(state, showMoney = true, Modifier.padding(8.dp)) }
            }
        }
    }
}

private fun demoConfig(n: Int): GameConfig = when (n) {
    40 -> Preset.CLASSIC.load()
    44 -> Preset.TIO_RICO.load()
    else -> resized(Preset.CLASSIC.load(), n)
}

/** Quita casillas repartidas por el anillo (nunca la salida ni las de la Cárcel) o añade descansos. */
private fun resized(base: GameConfig, n: Int): GameConfig {
    val squares = base.squares.toMutableList()
    val extra = n - squares.size
    if (extra < 0) {
        val removable = squares.indices.filter { squares[it] !is Start && squares[it] !is Jail && squares[it] !is GoToJail }
        val drop = (0 until -extra).map { removable[it * removable.size / -extra] }.toSet()
        return base.copy(squares = squares.filterIndexed { i, _ -> i !in drop })
    }
    repeat(extra) { j -> squares.add((j + 1) * base.squares.size / (extra + 1) + j, Rest("Descanso")) }
    return base.copy(squares = squares)
}

private val names = listOf("Andrés", "Santi", "Gabi", "Ximena", "Eva", "Fede")

/** Estado inicial del motor con fichas, dueños, casas, un hotel y una hipoteca puestos a mano. */
private fun demoState(config: GameConfig, players: Int): GameState {
    val start = Engine.newGame(config, names.take(players), seed = 7).state
    val n = config.squares.size
    val spots = (0 until players).map { p -> if (p == 2) (n / players + 5) % n else (p * n / players + 5) % n }
    val ownable = config.squares.indices.filter { config.squares[it] is OwnableSquare }
    val holdings = ownable.filterIndexed { k, _ -> k % 3 != 2 }.mapIndexed { k, sq ->
        val property = config.squares[sq] is Property
        sq to when {
            property && k % 7 == 5 -> Holding(owner = k % players, hotel = true)
            k % 7 == 6 -> Holding(owner = k % players, mortgaged = true)
            property -> Holding(owner = k % players, houses = k % 5)
            else -> Holding(owner = k % players)
        }
    }.toMap()
    return start.copy(
        players = start.players.mapIndexed { p, s -> s.copy(position = spots[p], money = s.money - 137 * p) },
        holdings = holdings,
        current = 0,
    )
}
