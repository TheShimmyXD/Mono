package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.DialogoChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.board.Board
import com.jacck.mono.board.Icon
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.board.RingGrid
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding

/**
 * Maquetas de FC.2 (D-58): el panel de jugadores solo con sus íconos, con datos de muestra (4 jugadores,
 * turno de Ana). Extra `maqueta`: A, en fila sin dinero; B, en fila con el dinero debajo; C, en columna
 * sin dinero; D, en columna con el dinero al lado; E, la tarjeta que abre un ícono (Ana) con sus
 * casillas marcadas en el tablero.
 */
@Composable
fun Maqueta(letra: String) {
    val config = remember { Preset.CLASSIC.load() }
    val state = remember {
        val s = Engine.newGame(config, listOf("Ana", "Botty 1", "Caro", "Dani"), 1L).state
        val money = listOf(1240, 860, 1510, 395)
        s.copy(
            current = 0,
            players = s.players.mapIndexed { i, p -> p.copy(money = money[i]) },
            holdings = mapOf(
                5 to Holding(0), 6 to Holding(0, houses = 1), 8 to Holding(0), 9 to Holding(0),
                21 to Holding(2), 23 to Holding(2), 24 to Holding(2), 15 to Holding(1), 37 to Holding(3, mortgaged = true),
            ),
        )
    }
    val titulo = when (letra) {
        "B" -> "B · En fila, con dinero"
        "C" -> "C · En columna, sin dinero"
        "D" -> "D · En columna, con dinero"
        "E" -> "E · Al tocar a Ana"
        else -> "A · En fila, sin dinero"
    }
    PantallaChiva {
        BoxWithConstraints(Modifier.fillMaxSize().padding(4.dp)) {
            Board(config, state, Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(2.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
                    Text(titulo, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    when (letra) {
                        "C", "D" -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Calcomania(Modifier.width(if (letra == "D") 132.dp else 72.dp)) {
                                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.players.indices.forEach { k ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icono(state, k, 44.dp)
                                            if (letra == "D") Text("$${state.players[k].money}", fontSize = 15.sp, modifier = Modifier.padding(start = 6.dp))
                                        }
                                    }
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Calcomania { Turno(state) }
                                BotonChiva("Tirar los dados", {})
                            }
                        }
                        else -> {
                            Calcomania {
                                Turno(state)
                                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    state.players.indices.forEach { k ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icono(state, k, 48.dp)
                                            if (letra == "B") Text("$${state.players[k].money}", fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                            BotonChiva("Tirar los dados", {})
                        }
                    }
                }
            }
            if (letra == "E") {
                // Las casillas de Ana, con un marco de su color (FC.3).
                val n = config.squares.size
                val grid = remember(n, maxWidth, maxHeight) { RingGrid.fit(n, maxWidth.value, maxHeight.value) }
                val cw = maxWidth / grid.cols
                val ch = maxHeight / grid.rows
                state.holdings.filterValues { it.owner == 0 }.keys.forEach { i ->
                    val c = grid.cellOf(i)
                    Box(Modifier.offset(cw * c.col, ch * c.row).size(cw, ch).border(4.dp, PlayerColors[0]))
                }
            }
        }
    }
    if (letra == "E") {
        val ana = state.players[0]
        DialogoChiva("Ana", botones = {
            BotonChiva("Propiedades (${state.holdings.count { it.value.owner == 0 }})", {}, icono = Icon.CASA)
            BotonChiva("Cerrar", {}, principal = false)
        }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PlayerToken(state, 0, 64.dp)
                Column {
                    Text("$${ana.money}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Le toca a Ana", fontSize = 15.sp)
                    Text("Sus casillas tienen marco rojo", fontSize = 15.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun Turno(state: GameState) =
    Text("Turno de ${state.players[state.current].name}", fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))

/** El ícono de un jugador; el de quien juega, un poco más grande, con fondo y aro. */
@Composable
private fun Icono(state: GameState, k: Int, size: Dp) {
    val turno = k == state.current
    Box(
        Modifier.background(if (turno) Chiva.Turno else Color.Transparent, CircleShape)
            .border(if (turno) 2.5.dp else 0.dp, if (turno) Chiva.Tinta else Color.Transparent, CircleShape).padding(4.dp)
            .alpha(if (state.players[k].bankrupt) 0.4f else 1f),
        contentAlignment = Alignment.BottomEnd,
    ) {
        PlayerToken(state, k, if (turno) size * 1.15f else size)
    }
}
