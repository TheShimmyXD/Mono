package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Maquetas de FC.2, segunda vuelta (D-58): el panel de jugadores solo con sus íconos, con datos de muestra
 * (4 jugadores, turno de Ana). Extra `maqueta`: A y B, en fila pegados arriba (sin y con dinero); C y D,
 * dos a cada lado (sin y con dinero); E, la tarjeta de Ana en el centro, sin oscurecer, con sus casillas marcadas.
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
        "B" -> "B · Arriba, con dinero"
        "C" -> "C · A los lados, sin dinero"
        "D" -> "D · A los lados, con dinero"
        "E" -> "E · Tarjeta en el centro (Ana)"
        else -> "A · Arriba, sin dinero"
    }
    val conDinero = letra == "B" || letra == "D"
    PantallaChiva {
        BoxWithConstraints(Modifier.fillMaxSize().padding(4.dp)) {
            Board(config, state, Modifier.fillMaxSize()) {
                if (letra == "C" || letra == "D") {
                    // Dos a cada lado, pegados a las columnas del tablero.
                    Row(Modifier.fillMaxSize()) {
                        Lado(state, listOf(0, 1), conDinero)
                        Medio(state, titulo, Modifier.weight(1f))
                        Lado(state, listOf(2, 3), conDinero)
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        // En fila, pegados arriba.
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            state.players.indices.forEach { k ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icono(state, k, 46.dp)
                                    if (conDinero) Dinero(state, k)
                                }
                            }
                        }
                        if (letra == "E") Tarjeta(state, titulo, Modifier.weight(1f)) else Medio(state, titulo, Modifier.weight(1f))
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
}

/** Una columna de íconos a un lado del centro, arriba. */
@Composable
private fun Lado(state: GameState, players: List<Int>, conDinero: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        players.forEach { k ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icono(state, k, 46.dp)
                if (conDinero) Dinero(state, k)
            }
        }
    }
}

@Composable
private fun Dinero(state: GameState, k: Int) =
    Text("$${state.players[k].money}", fontSize = 14.sp, fontWeight = FontWeight.Bold)

/** El centro: el título de la maqueta, de quién es el turno y «Tirar». */
@Composable
private fun Medio(state: GameState, titulo: String, modifier: Modifier) {
    Column(modifier.fillMaxHeight().padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        Text(titulo, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Calcomania { Turno(state) }
        BotonChiva("Tirar los dados", {})
    }
}

/** La tarjeta de Ana en el centro, sin oscurecer el tablero: sus casillas se ven marcadas. */
@Composable
private fun Tarjeta(state: GameState, titulo: String, modifier: Modifier) {
    val ana = state.players[0]
    Column(modifier.fillMaxHeight().padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
        Text(titulo, fontSize = 15.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Calcomania {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PlayerToken(state, 0, 64.dp)
                Column {
                    Text(ana.name, fontSize = 22.sp)
                    Text("$${ana.money}", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("Sus casillas: marco rojo", fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
                }
            }
        }
        BotonChiva("Propiedades (${state.holdings.count { it.value.owner == 0 }})", {}, icono = Icon.CASA)
        BotonChiva("Cerrar", {}, principal = false)
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
