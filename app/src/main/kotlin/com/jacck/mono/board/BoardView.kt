package com.jacck.mono.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.Chiva
import com.jacck.mono.R
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Square

/** Color de la ficha de cada jugador (2-6, D-05). */
val PlayerColors = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFFB300, 0xFF8E24AA, 0xFF00ACC1).map { Color(it) }

/**
 * El tablero en anillo (D-20) dibujado desde la configuración y el estado de la partida, para
 * cualquier N; [center] va dentro del anillo (dados, casilla, jugadores, botones).
 */
@Composable
fun Board(
    config: GameConfig, state: GameState, modifier: Modifier = Modifier, onSquare: (Int) -> Unit = {},
    center: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        val n = config.squares.size
        val grid = remember(n, maxWidth, maxHeight) { RingGrid.fit(n, maxWidth.value, maxHeight.value) }
        val cw = maxWidth / grid.cols
        val ch = maxHeight / grid.rows
        val current = state.players[state.current].position
        for (i in 0 until n) {
            val at = grid.cellOf(i)
            SquareCell(
                config = config,
                square = config.squares[i],
                holding = state.holdings[i],
                tokens = state.players.indices.filter { state.players[it].position == i && !state.players[it].bankrupt },
                highlighted = i == current,
                width = cw,
                modifier = Modifier.offset(cw * at.col, ch * at.row).size(cw, ch).clickable { onSquare(i) },
            )
        }
        Box(Modifier.offset(cw, ch).size(cw * (grid.cols - 2), ch * (grid.rows - 2)).padding(6.dp)) { center() }
    }
}

@Composable
private fun SquareCell(
    config: GameConfig, square: Square, holding: Holding?, tokens: List<Int>, highlighted: Boolean, width: Dp,
    modifier: Modifier,
) {
    // Letra y fichas crecen con la casilla: de 49 dp (N = 48) a 78 dp (N = 16) en el Redmi.
    val text = (width.value / 7f).coerceIn(7f, 12f).sp
    val token = minOf(12.dp, (width - 6.dp) / 6)
    Column(
        modifier
            .background(if (highlighted) Chiva.Turno else Color.White)
            .border(if (highlighted) 3.dp else 1.5.dp, Chiva.Tinta),
    ) {
        groupColor(config, square)?.let { Box(Modifier.fillMaxWidth().height(width / 6).background(it).border(0.75.dp, Chiva.Tinta)) }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 2.dp).alpha(if (holding?.mortgaged == true) 0.35f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val icon = square.icon()
            if (icon != null) {
                IconImage(icon, minOf(width * 0.5f, 32.dp))
            } else {
                Text(
                    square.name, fontSize = text, lineHeight = text * 1.1f, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                )
            }
            if (square is OwnableSquare && holding == null) {
                Text(stringResource(R.string.money, square.price), fontSize = text, fontWeight = FontWeight.Bold)
            }
            holding?.let { Buildings(it, text) }
        }
        if (tokens.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(bottom = 2.dp), horizontalArrangement = Arrangement.Center) {
                tokens.forEach { Token(PlayerColors[it], token) }
            }
        }
        holding?.let { Box(Modifier.fillMaxWidth().height(4.dp).background(PlayerColors[it.owner])) }
    }
}

/** Casas, hotel o hipoteca de una casilla con dueño (R-25, R-27, R-31). */
@Composable
private fun Buildings(holding: Holding, text: androidx.compose.ui.unit.TextUnit) {
    if (holding.mortgaged) Text(stringResource(R.string.mortgaged_short), fontSize = text, maxLines = 1)
    else BuildingIcons(holding, (text.value * 1.3f).dp)
}

@Composable
fun Token(color: Color, size: Dp) {
    Box(Modifier.padding(horizontal = 1.dp).size(size).background(color, CircleShape).border(1.dp, Color.Black, CircleShape))
}

/** Medallón de un personaje (FB.3, D-35, opción C): disco blanco con aro del color del jugador. */
@Composable
fun Medallon(pj: Int, color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier.size(size).clip(CircleShape).background(Color.White)
            .border(size / 10, color, CircleShape).border(size / 35, Chiva.Tinta, CircleShape),
    ) {
        Image(painterResource(pj), null, Modifier.size(size).padding(size / 8))
    }
}

/** Jugadores con su ficha y su dinero; el de turno, marcado. Va dentro del panel del centro (D-33). */
@Composable
fun PlayersPanel(state: GameState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        state.players.forEachIndexed { k, p ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp).alpha(if (p.bankrupt) 0.4f else 1f)) {
                Token(PlayerColors[k], 14.dp)
                Spacer(Modifier.width(6.dp))
                if (k == state.current) IconImage(Icon.TURNO, 14.dp, Modifier.padding(end = 3.dp))
                Text(
                    p.name,
                    fontWeight = if (k == state.current) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f), fontSize = 15.sp,
                )
                Text(stringResource(R.string.money, p.money), fontSize = 15.sp)
            }
        }
    }
}

private fun groupColor(config: GameConfig, square: Square): Color? =
    (square as? Property)?.let { p -> config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) }
