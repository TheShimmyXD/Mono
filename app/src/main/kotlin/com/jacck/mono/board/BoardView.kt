package com.jacck.mono.board

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp

/** Color de la ficha de cada jugador (2-6, D-05). */
val PlayerColors = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFFB300, 0xFF8E24AA, 0xFF00ACC1).map { Color(it) }

/**
 * Ficha en el aire (FC.1, D-59): va de la casilla [from] a [to] y lleva [f] (0 a 1) del saltito.
 * Con `from == to` está quieta ahí (espera su turno de caminar).
 */
data class Hop(val from: Int, val to: Int, val f: Float = 0f)

/**
 * Casilla que vuela (FD.1, D-65): [square] pasa a [player]; [f] va de 0 a 1. Hasta [tint] toma el color
 * del jugador; después se encoge y va hasta su ícono, donde se desvanece.
 */
data class Fly(val square: Int, val player: Int, val f: Float, val tint: Float)

/**
 * Billetes que van de un ícono a otro (FD.2, D-66): por cada par de [lanes] (quien paga, quien cobra),
 * [BILLS] billetes del color de quien paga; [f] va de 0 a 1.
 */
data class Bills(val lanes: List<Pair<Int, Int>>, val f: Float)

/** Billetes por pago, qué parte de la animación tarda cada uno en cruzar y cuánto sube su arco (D-66). */
const val BILLS = 5
const val BILL_TRIP = 0.5f
const val BILL_ARC = 22f

/** Dónde quedó dibujado el tablero, para llevar la casilla que vuela hasta el ícono (D-65). */
private class Where {
    var at: LayoutCoordinates? = null
}

/** Respiración de las casillas marcadas (D-62): ms en crecer (y otros tantos en volver) y cuánto crecen. */
const val BREATH_MS = 1400
const val BREATH_SCALE = 0.12f

/**
 * El tablero en anillo (D-20) dibujado desde la configuración y el estado de la partida, para
 * cualquier N; [center] va dentro del anillo (dados, casilla, jugadores, botones). Las fichas de
 * [hops] (por jugador) no van en su casilla del estado: se dibujan donde dice su [Hop] (FC.1, D-59).
 * Las casillas de [marks] llevan un marco de [markColor] (las de un jugador elegido, FC.3, D-60) y
 * respiran: se agrandan y vuelven, con una máscara translúcida de ese color encima (D-62).
 * [flight] es la casilla que vuela al ícono de quien la compró (D-65); [iconOf] dice dónde quedó
 * dibujado el ícono de cada jugador (`PlayersRow`, en [center]). [bills] son los billetes de un pago
 * entre jugadores (D-66).
 */
@Composable
fun Board(
    config: GameConfig, state: GameState, modifier: Modifier = Modifier, onSquare: (Int) -> Unit = {},
    highlight: Int? = state.players[state.current].position, showTokens: Boolean = true, hops: Map<Int, Hop> = emptyMap(),
    marks: Set<Int> = emptySet(), markColor: Color = Chiva.Tinta, flight: Fly? = null, bills: Bills? = null,
    iconOf: (Int) -> LayoutCoordinates? = { null }, center: @Composable () -> Unit,
) {
    val where = remember { Where() }
    val density = LocalDensity.current
    // Centro del ícono de [k] en el tablero, o null si aún no se dibujó.
    fun iconAt(k: Int): Pair<Dp, Dp>? = where.at?.let { me ->
        iconOf(k)?.takeIf { it.isAttached }?.let { me.localPositionOf(it, Offset(it.size.width / 2f, it.size.height / 2f)) }
    }?.let { with(density) { it.x.toDp() to it.y.toDp() } }
    BoxWithConstraints(modifier.onGloballyPositioned { where.at = it }) {
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
        if (marks.isNotEmpty()) {
            // Encima de las demás, para que al crecer no las tape la vecina (D-62).
            val breath by rememberInfiniteTransition(label = "respira").animateFloat(
                0f, 1f, infiniteRepeatable(tween(BREATH_MS, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "respira",
            )
            marks.forEach { i ->
                val at = grid.cellOf(i)
                val k = 1f + BREATH_SCALE * breath
                Box(Modifier.offset(cw * at.col, ch * at.row).size(cw, ch).graphicsLayer { scaleX = k; scaleY = k }.clickable { onSquare(i) }) {
                    SquareCell(
                        config, state, config.squares[i], state.holdings[i],
                        if (showTokens) state.players.indices.filter { state.players[it].position == i && !state.players[it].bankrupt && it !in hops } else emptyList(),
                        i == highlight, cw, Modifier.fillMaxSize(),
                    )
                    Box(Modifier.fillMaxSize().background(markColor.copy(alpha = 0.2f + 0.2f * breath)).border(4.dp, markColor))
                }
            }
        }
        Box(Modifier.offset(cw, ch).size(cw * (grid.cols - 2), ch * (grid.rows - 2)).padding(6.dp)) { center() }
        flight?.let { fly ->
            val at = grid.cellOf(fly.square)
            val color = PlayerColors[fly.player]
            val t = (fly.f / fly.tint).coerceIn(0f, 1f)
            val g = FastOutSlowInEasing.transform(((fly.f - fly.tint) / (1f - fly.tint)).coerceIn(0f, 1f))
            // Del centro de la casilla al del ícono (si aún no se dibujó, se queda en su sitio).
            val x0 = cw * at.col + cw / 2
            val y0 = ch * at.row + ch / 2
            val (x1, y1) = iconAt(fly.player) ?: (x0 to y0)
            val k = (1f + 0.08f * t) * (1f - 0.75f * g)
            Box(
                Modifier.offset(lerp(x0, x1, g) - cw / 2, lerp(y0, y1, g) - ch / 2).size(cw, ch)
                    .graphicsLayer { scaleX = k; scaleY = k; alpha = ((1f - g) / 0.2f).coerceIn(0f, 1f) },
            ) {
                SquareCell(config, state, config.squares[fly.square], state.holdings[fly.square], emptyList(), false, cw, Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(color.copy(alpha = 0.85f * t)).border(4.dp, color))
            }
        }
        bills?.lanes?.forEach { (from, to) ->
            val a = iconAt(from) ?: return@forEach
            val b = iconAt(to) ?: return@forEach
            // Salen escalonados y cruzan en arco, apareciendo y desvaneciéndose en las puntas.
            for (i in 0 until BILLS) {
                val start = (1f - BILL_TRIP - 0.1f) * i / (BILLS - 1)
                val g = ((bills.f - start) / BILL_TRIP).coerceIn(0f, 1f)
                if (g <= 0f || g >= 1f) continue
                val e = FastOutSlowInEasing.transform(g)
                val x = lerp(a.first, b.first, e)
                val y = lerp(a.second, b.second, e) - (sin(PI * e).toFloat() * BILL_ARC).dp
                Bill(PlayerColors[from], Modifier.offset(x - 12.dp, y - 7.dp).alpha(minOf(1f, g / 0.15f, (1f - g) / 0.15f)))
            }
        }
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

/** Un billete del color de quien paga (D-66), con su signo de dinero. */
@Composable
private fun Bill(color: Color, modifier: Modifier) {
    Box(
        modifier.size(24.dp, 14.dp).background(color, RoundedCornerShape(2.dp)).border(1.5.dp, Chiva.Tinta, RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("$", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, lineHeight = 9.sp)
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

/** En un pago (D-66): cuánto engorda quien cobra y se encoge quien paga, y qué parte tarda en hacerlo. */
const val SWELL = 0.3f
const val SHRINK = 0.2f
const val SWELL_EDGE = 0.2f

/** Color del dinero mientras sube y mientras baja (D-66). */
val MoneyUp = Color(0xFF1E8E3E)
val MoneyDown = Chiva.Techo

/**
 * Los jugadores en fila (FC.3, D-60): su ícono y, debajo, su dinero ([money], el que se ve) si
 * [showMoney]; el de quien juega, 15 % más grande, con fondo y aro. Un toque es [onTap] y una
 * pulsación larga [onLongPress]. Mientras se anima un pago ([swing]: lo que gana o pierde cada uno;
 * [swingF] de 0 a 1, D-66), quien cobra engorda y quien paga se encoge, su dinero va de verde o rojo y
 * debajo sale «+$x» o «−$x», sin mover lo de abajo.
 */
@Composable
fun PlayersRow(
    state: GameState, showMoney: Boolean, modifier: Modifier = Modifier, size: Dp = 46.dp,
    money: List<Int> = state.players.map { it.money }, swing: Map<Int, Int> = emptyMap(), swingF: Float = 0f,
    onTap: (Int) -> Unit = {}, onLongPress: () -> Unit = {}, onPlaced: (Int, LayoutCoordinates) -> Unit = { _, _ -> },
) {
    val going = swingF > 0f && swingF < 1f
    val e = FastOutSlowInEasing.transform(minOf(1f, swingF / SWELL_EDGE, (1f - swingF) / SWELL_EDGE).coerceIn(0f, 1f))
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
        state.players.forEachIndexed { k, p ->
            val turn = k == state.current
            val d = swing[k]?.takeIf { going && it != 0 }
            val tint = when {
                d == null -> Color.Unspecified
                d > 0 -> MoneyUp
                else -> MoneyDown
            }
            val grow = when {
                d == null -> 1f
                d > 0 -> 1f + SWELL * e
                else -> 1f - SHRINK * e
            }
            Column(
                Modifier.zIndex(if (d != null) 1f else 0f).combinedClickable(onLongClick = onLongPress) { onTap(k) }
                    .alpha(if (p.bankrupt) 0.4f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.graphicsLayer { scaleX = grow; scaleY = grow }
                        .background(if (turn) Chiva.Turno else Color.Transparent, CircleShape)
                        .border(if (turn) 2.5.dp else 0.dp, if (turn) Chiva.Tinta else Color.Transparent, CircleShape).padding(4.dp),
                ) {
                    PlayerToken(state, k, if (turn) size * 1.15f else size, Modifier.onGloballyPositioned { onPlaced(k, it) })
                }
                if (showMoney) Text(stringResource(R.string.money, money[k]), color = tint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                // Alto cero: la etiqueta cuelga debajo sin empujar el centro del tablero.
                if (d != null) Box(Modifier.height(0.dp)) {
                    Text(
                        stringResource(if (d > 0) R.string.pay_plus else R.string.pay_minus, abs(d)), color = tint,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.wrapContentHeight(Alignment.Top, unbounded = true),
                    )
                }
            }
        }
    }
}

private fun groupColor(config: GameConfig, square: Square): Color? =
    (square as? Property)?.let { p -> config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) }
