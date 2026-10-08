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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** Color de la ficha de cada jugador (2-6, D-05). */
val PlayerColors = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFFB300, 0xFF8E24AA, 0xFF00ACC1).map { Color(it) }

/**
 * Ficha en el aire (FC.1, D-59): va de la casilla [from] a [to] y lleva [f] (0 a 1) del saltito.
 * Con `from == to` está quieta ahí (espera su turno de caminar).
 */
data class Hop(val from: Int, val to: Int, val f: Float = 0f)

/**
 * El tablero en anillo (D-20) dibujado desde la configuración y el estado de la partida, para
 * cualquier N; [center] va dentro del anillo (dados, casilla, jugadores, botones). Las fichas de
 * [hops] (por jugador) no van en su casilla del estado: se dibujan donde dice su [Hop] (FC.1, D-59).
 */
@Composable
fun Board(
    config: GameConfig, state: GameState, modifier: Modifier = Modifier, onSquare: (Int) -> Unit = {},
    highlight: Int? = state.players[state.current].position, showTokens: Boolean = true, hops: Map<Int, Hop> = emptyMap(),
    center: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        val n = config.squares.size
        val grid = remember(n, maxWidth, maxHeight) { RingGrid.fit(n, maxWidth.value, maxHeight.value) }
        val cw = maxWidth / grid.cols
        val ch = maxHeight / grid.rows
        for (i in 0 until n) {
            val at = grid.cellOf(i)
            SquareCell(
                config = config,
                state = state,
                square = config.squares[i],
                holding = state.holdings[i],
                tokens = if (showTokens) state.players.indices.filter { state.players[it].position == i && !state.players[it].bankrupt && it !in hops } else emptyList(),
                highlighted = i == highlight,
                width = cw,
                modifier = Modifier.offset(cw * at.col, ch * at.row).size(cw, ch).clickable { onSquare(i) },
            )
        }
        Box(Modifier.offset(cw, ch).size(cw * (grid.cols - 2), ch * (grid.rows - 2)).padding(6.dp)) { center() }
        if (showTokens) hops.forEach { (k, hop) ->
            val size = minOf(30.dp, cw * 0.48f)
            val a = grid.cellOf(hop.from)
            val b = grid.cellOf(hop.to)
            // Sube y baja (seno) mientras cruza; el salto a la Cárcel, que cruza el tablero, sube más.
            val alto = sin(PI * hop.f).toFloat() * if (abs(b.col - a.col) + abs(b.row - a.row) > 1) 2f else 0.9f
            val x = cw * (a.col + (b.col - a.col) * hop.f) + 2.dp
            val y = ch * (a.row + (b.row - a.row) * hop.f) + ch - size - 6.dp - size * alto
            PlayerToken(state, k, size, Modifier.offset(x, y).scale(1f + 0.12f * alto))
        }
    }
}

@Composable
private fun SquareCell(
    config: GameConfig, state: GameState, square: Square, holding: Holding?, tokens: List<Int>, highlighted: Boolean, width: Dp,
    modifier: Modifier,
) {
    // Letra y fichas crecen con la casilla: de 49 dp (N = 48) a 78 dp (N = 16) en el Redmi.
    val text = (width.value / 7f).coerceIn(7f, 12f).sp
    Box(modifier) {
        Column(
            Modifier.fillMaxSize()
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
                    NombreCasilla(square.name, text)
                }
                if (square is OwnableSquare && holding == null) {
                    Text(stringResource(R.string.money, square.price), fontSize = text, fontWeight = FontWeight.Bold)
                }
                holding?.let { Buildings(it, text) }
            }
            holding?.let { Box(Modifier.fillMaxWidth().height(4.dp).background(PlayerColors[it.owner])) }
        }
        if (tokens.isNotEmpty()) Medallones(state, tokens, width, Modifier.align(Alignment.BottomStart).padding(bottom = 6.dp))
    }
}

/**
 * El nombre de la casilla con la letra más grande, de [max] a 5.5 sp, en la que cada palabra cabe
 * entera y el nombre en dos líneas: sin esto, «Teusaquillo» salía partido en celdas de 35 dp (F4.3).
 */
@Composable
private fun NombreCasilla(name: String, max: TextUnit) {
    val measurer = rememberTextMeasurer()
    val base = LocalTextStyle.current
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val width = constraints.maxWidth
        val size = remember(name, width, max) {
            var sp = max.value
            while (sp > 5.5f) {
                val style = base.copy(fontSize = sp.sp, lineHeight = (sp * 1.1f).sp)
                val words = name.split(' ').all { measurer.measure(it, style, softWrap = false, maxLines = 1).size.width <= width }
                if (words && !measurer.measure(name, style, maxLines = 2, constraints = Constraints(maxWidth = width)).hasVisualOverflow) break
                sp -= 0.5f
            }
            sp.sp
        }
        Text(
            name, fontSize = size, lineHeight = size * 1.1f, maxLines = 2,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
        )
    }
}

/** Casas, hotel o hipoteca de una casilla con dueño (R-25, R-27, R-31). */
@Composable
private fun Buildings(holding: Holding, text: androidx.compose.ui.unit.TextUnit) {
    if (holding.mortgaged) Text(stringResource(R.string.mortgaged_short), fontSize = text, maxLines = 1)
    else BuildingIcons(holding, (text.value * 1.3f).dp)
}

/** Dibujo del personaje de la posición [k] (FB.4, D-36); sin personaje (partida de antes), el de su posición. */
fun personaje(token: String?, k: Int): Int =
    Personajes.firstOrNull { it.first == token }?.second ?: Personajes[k % Personajes.size].second

/** Medallón del jugador [k]: su personaje con el aro de su color (D-35, D-36). */
@Composable
fun PlayerToken(state: GameState, k: Int, size: Dp, modifier: Modifier = Modifier) =
    Medallon(personaje(state.players[k].token, k), PlayerColors[k], size, modifier)

/** Fichas de una casilla (D-35, opción C): medallones encimados abajo, de izquierda a derecha. */
@Composable
private fun Medallones(state: GameState, tokens: List<Int>, width: Dp, modifier: Modifier) {
    val size = minOf(30.dp, width * 0.48f)
    val step = if (tokens.size > 1) minOf(size, (width - 4.dp - size) / (tokens.size - 1)) else 0.dp
    Box(modifier.fillMaxWidth().height(size)) {
        tokens.forEachIndexed { i, k -> PlayerToken(state, k, size, Modifier.offset(x = 2.dp + step * i)) }
    }
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
                PlayerToken(state, k, 20.dp)
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
