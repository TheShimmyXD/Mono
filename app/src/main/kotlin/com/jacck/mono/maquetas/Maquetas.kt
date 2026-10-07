package com.jacck.mono.maquetas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.CardSquare
import com.jacck.mono.engine.model.Deck
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GoToJail
import com.jacck.mono.engine.model.Jail
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Rest
import com.jacck.mono.engine.model.Square
import com.jacck.mono.engine.model.Start
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility
import kotlin.math.cos
import kotlin.math.sin

// Maquetas de F3.1: tres formas de dibujar el anillo con datos falsos sobre el preset Clásico.
// Son desechables: F3.2 programa la elegida con su geometría probada y este archivo se borra.

private data class FakePlayer(val name: String, val money: Int, val position: Int, val color: Color)

private val players = listOf(
    FakePlayer("Ana", 1320, 16, Color(0xFFE53935)),
    FakePlayer("Beto", 980, 5, Color(0xFF1E88E5)),
    FakePlayer("Caro", 1610, 24, Color(0xFF43A047)),
    FakePlayer("Dani", 745, 16, Color(0xFFFFB300)),
)
private const val CURRENT = 0

/** Casilla → dueño (índice en [players]). */
private val owners = mapOf(1 to 1, 3 to 1, 6 to 0, 11 to 2, 13 to 2, 15 to 0, 19 to 3, 21 to 2, 25 to 1, 37 to 3, 39 to 3)

private val boardBg = Color(0xFFF4EFE1)
private val neutral = Color(0xFFDCD5C0)

/** Entrada de las maquetas: "A", "B" o "C" (extra `maqueta` del intent). */
@Composable
fun Maqueta(which: String) {
    val config = remember { Preset.CLASSIC.load() }
    Box(Modifier.fillMaxSize().background(Color(0xFF2E5E4E)).safeDrawingPadding()) {
        when (which) {
            "B" -> MaquetaB(config)
            "C" -> MaquetaC(config)
            else -> MaquetaA(config)
        }
    }
}

// --- A: cuadrado clásico de 11 × 11, panel debajo ---

@Composable
private fun MaquetaA(config: GameConfig) {
    Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RectRing(config, cols = 11, rows = 11, compact = true, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("MONO", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E5E4E))
                Text("⚂ ⚄", fontSize = 40.sp)
            }
        }
        SquareCard(config, players[CURRENT].position)
        PlayersPanel()
        Actions()
    }
}

// --- B: rectángulo alto de 7 × 15 que llena la pantalla, panel en el centro ---

@Composable
private fun MaquetaB(config: GameConfig) {
    RectRing(config, cols = 7, rows = 15, compact = false, modifier = Modifier.fillMaxSize().padding(2.dp)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            Text("⚂ ⚄", fontSize = 44.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            SquareCard(config, players[CURRENT].position)
            PlayersPanel()
            Actions()
        }
    }
}

// --- C: anillo circular, ficha de la casilla en el centro ---

@Composable
private fun MaquetaC(config: GameConfig) {
    Column(Modifier.fillMaxSize().padding(6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        CircleRing(config, Modifier.fillMaxWidth().aspectRatio(1f))
        Text("⚂ ⚄", fontSize = 36.sp, color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
        PlayersPanel()
        Actions()
    }
}

// --- Piezas comunes ---

/** (columna, fila) de la casilla [i] en un anillo de [cols] × [rows]; la salida abajo a la derecha, avanza a la izquierda. */
private fun ringCell(i: Int, cols: Int, rows: Int): Pair<Int, Int> = when {
    i < cols -> Pair(cols - 1 - i, rows - 1)
    i < cols + rows - 1 -> Pair(0, rows - 1 - (i - cols + 1))
    i < 2 * cols + rows - 2 -> Pair(i - (cols + rows - 2), 0)
    else -> Pair(cols - 1, i - (2 * cols + rows - 3))
}

@Composable
private fun RectRing(
    config: GameConfig, cols: Int, rows: Int, compact: Boolean, modifier: Modifier,
    center: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier.background(boardBg)) {
        val cw = maxWidth / cols
        val ch = maxHeight / rows
        for (i in config.squares.indices) {
            val (c, r) = ringCell(i, cols, rows)
            Cell(config, i, compact, Modifier.offset(cw * c, ch * r).size(cw, ch))
        }
        Box(Modifier.offset(cw, ch).size(cw * (cols - 2), ch * (rows - 2)).padding(6.dp)) { center() }
    }
}

@Composable
private fun Cell(config: GameConfig, index: Int, compact: Boolean, modifier: Modifier) {
    val sq = config.squares[index]
    val here = players.filter { it.position == index }
    val isCurrent = players[CURRENT].position == index
    Column(
        modifier
            .border(if (isCurrent) 2.dp else 0.5.dp, if (isCurrent) Color.Black else Color.Gray)
            .background(boardBg),
    ) {
        groupColor(config, sq)?.let { Box(Modifier.fillMaxWidth().height(if (compact) 6.dp else 9.dp).background(it)) }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            val emoji = sq.emoji()
            when {
                emoji != null -> Text(emoji, fontSize = if (compact) 13.sp else 18.sp)
                compact -> Text("${(sq as OwnableSquare).price}", fontSize = 8.sp)
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        sq.name, fontSize = 7.sp, lineHeight = 8.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    Text("$${(sq as OwnableSquare).price}", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (here.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                here.forEach { Token(it.color, if (compact) 7.dp else 10.dp) }
            }
        }
        owners[index]?.let { Box(Modifier.fillMaxWidth().height(3.dp).background(players[it].color)) }
    }
}

@Composable
private fun Token(color: Color, size: Dp) {
    Box(Modifier.padding(horizontal = 1.dp).size(size).background(color, CircleShape).border(1.dp, Color.Black, CircleShape))
}

@Composable
private fun CircleRing(config: GameConfig, modifier: Modifier) {
    val n = config.squares.size
    val step = 360f / n
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight)
        val thick = 38.dp
        val outer = side / 2 - 4.dp
        val mid = outer - thick / 2
        Canvas(Modifier.fillMaxSize()) {
            val t = thick.toPx()
            val m = mid.toPx()
            drawCircle(boardBg, radius = outer.toPx())
            for (i in 0 until n) {
                val start = 90f + i * step - step / 2
                val color = groupColor(config, config.squares[i]) ?: neutral
                drawArc(
                    color, start + 0.7f, step - 1.4f, useCenter = false,
                    topLeft = Offset(center.x - m, center.y - m), size = Size(2 * m, 2 * m), style = Stroke(t),
                )
                owners[i]?.let {
                    val r = m - t / 2 - 4.dp.toPx()
                    drawArc(
                        players[it].color, start + 1f, step - 2f, useCenter = false,
                        topLeft = Offset(center.x - r, center.y - r), size = Size(2 * r, 2 * r), style = Stroke(5.dp.toPx()),
                    )
                }
            }
            // La casilla del jugador en turno, resaltada.
            val cur = players[CURRENT].position
            drawArc(
                Color.Black, 90f + cur * step - step / 2, step, useCenter = false,
                topLeft = Offset(center.x - m, center.y - m), size = Size(2 * m, 2 * m), style = Stroke(t + 4.dp.toPx()),
            )
            drawArc(
                groupColor(config, config.squares[cur]) ?: neutral, 90f + cur * step - step / 2 + 1f, step - 2f,
                useCenter = false, topLeft = Offset(center.x - m, center.y - m), size = Size(2 * m, 2 * m), style = Stroke(t),
            )
            // Fichas por dentro del anillo.
            players.groupBy { it.position }.forEach { (pos, ps) ->
                ps.forEachIndexed { k, p ->
                    val a = Math.toRadians((90.0 + pos * step))
                    val r = m - t / 2 - 16.dp.toPx() - k * 14.dp.toPx()
                    val c = Offset(center.x + (r * cos(a)).toFloat(), center.y + (r * sin(a)).toFloat())
                    drawCircle(Color.Black, 7.dp.toPx(), c)
                    drawCircle(p.color, 6.dp.toPx(), c)
                }
            }
        }
        // Emojis de las casillas especiales sobre el anillo.
        for (i in 0 until n) {
            val e = config.squares[i].emoji() ?: continue
            val a = Math.toRadians(90.0 + i * step)
            Text(
                e, fontSize = 12.sp,
                modifier = Modifier.offset(mid * cos(a).toFloat(), mid * sin(a).toFloat()),
            )
        }
        Box(Modifier.size(side * 0.52f)) { SquareCard(config, players[CURRENT].position, Modifier.align(Alignment.Center)) }
    }
}

@Composable
private fun SquareCard(config: GameConfig, index: Int, modifier: Modifier = Modifier) {
    val sq = config.squares[index]
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
        groupColor(config, sq)?.let {
            Box(Modifier.fillMaxWidth().height(14.dp).background(it))
        }
        Column(Modifier.padding(8.dp)) {
            Text(sq.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (sq is OwnableSquare) Text("Precio $${sq.price}", fontSize = 13.sp)
            if (sq is Property) {
                Text("Alquiler $${sq.rents.first()} · 1 casa $${sq.rents.getOrNull(1) ?: "-"}", fontSize = 12.sp)
                sq.housePrice?.let { Text("Casa $$it", fontSize = 12.sp) }
            }
            owners[index]?.let { Text("Dueño: ${players[it].name}", fontSize = 12.sp, color = players[it].color) }
        }
    }
}

@Composable
private fun PlayersPanel() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            players.forEachIndexed { k, p ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Token(p.color, 12.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (k == CURRENT) "▶ ${p.name}" else p.name,
                        fontWeight = if (k == CURRENT) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f), fontSize = 14.sp,
                    )
                    Text("$${p.money}", fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun Actions() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Comprar") }
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(50))) { Text("Pasar") }
    }
}

private fun groupColor(config: GameConfig, sq: Square): Color? =
    (sq as? Property)?.let { p -> config.groups.first { it.id == p.group }.color }
        ?.let { Color(android.graphics.Color.parseColor(it)) }

private fun Square.emoji(): String? = when (this) {
    is Start -> "🏁"
    is Station -> "🚉"
    is Utility -> "💡"
    is Tax -> "💰"
    is CardSquare -> if (deck == Deck.A) "❓" else "🎁"
    is Jail -> "🔒"
    is GoToJail -> "🚓"
    is Rest -> "🅿️"
    is Property -> null
}
