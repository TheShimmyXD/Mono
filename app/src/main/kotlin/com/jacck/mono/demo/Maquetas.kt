package com.jacck.mono.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jacck.mono.R
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.RingGrid
import com.jacck.mono.board.icon
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.Preset
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property

/**
 * Maquetas de la pantalla que se está diseñando (M-029), con datos falsos; se abren con el extra
 * `maqueta`. FB.1: estilo de la partida (tablero, panel y diálogo de turno) en tres versiones del
 * «arte de chiva» (D-28): `A`, `B`, `C`; con `2` detrás (`A2`) sale encima el diálogo de compra.
 */
@Composable
fun Maqueta(letra: String) {
    val estilo = when (letra.first()) {
        'A' -> FIESTA
        'B' -> CARROCERIA
        else -> SOBRIA
    }
    val config = remember { Preset.CLASSIC.load() }
    val state = remember { partidaFalsa(config) }
    MaquetaPartida(estilo, config, state)
    if (letra.endsWith("2")) DialogoCompra(estilo, config, state)
}

private val Tinta = Color(0xFF1B1B1B)
private val Franja = listOf(0xFFE63946, 0xFFFFC21A, 0xFF1D7BEF, 0xFF2BB04A, 0xFFE5007E).map { Color(it) }
private val Lilita = FontFamily(Font(R.font.lilita_one))

/** Lo que cambia entre maquetas: colores, contorno, letra y forma de los botones. */
private class Estilo(
    val pantalla: Color,
    val casilla: Color,
    val contorno: Dp,
    val franjasPantalla: Boolean,
    val titulo: FontFamily,
    val texto: FontFamily,
    val panel: Color,
    val sombra: Boolean,
    val boton: Color,
    val botonTexto: Color,
    val secundario: Color,
    val forma: Shape,
    val marco: Color,
)

/** A · Chiva de fiesta: todo cargado, como la carrocería de una chiva. */
private val FIESTA = Estilo(
    pantalla = Color(0xFFFFC21A), casilla = Color.White, contorno = 1.5.dp, franjasPantalla = true,
    titulo = Lilita, texto = Lilita, panel = Color.White, sombra = true,
    boton = Color(0xFFE63946), botonTexto = Color.White, secundario = Color(0xFFFFD60A),
    forma = RoundedCornerShape(10.dp), marco = Tinta,
)

/** B · Carrocería azul: madera pintada; Lilita solo en títulos y botones. */
private val CARROCERIA = Estilo(
    pantalla = Color(0xFF1D7BEF), casilla = Color(0xFFF7D9A8), contorno = 1.dp, franjasPantalla = false,
    titulo = Lilita, texto = FontFamily.Default, panel = Color(0xFFFFF4DE), sombra = false,
    boton = Color(0xFF2BB04A), botonTexto = Color.White, secundario = Color(0xFFFFFFFF),
    forma = CircleShape, marco = Color(0xFFE5007E),
)

/** C · Sobria con acentos: el tablero de hoy con contorno negro; la chiva solo en panel y diálogos. */
private val SOBRIA = Estilo(
    pantalla = Color(0xFF2E5E4E), casilla = Color(0xFFF4EFE1), contorno = 0.75.dp, franjasPantalla = false,
    titulo = FontFamily.Default, texto = FontFamily.Default, panel = Color.White, sombra = false,
    boton = Color(0xFFE63946), botonTexto = Color.White, secundario = Color.White,
    forma = RoundedCornerShape(8.dp), marco = Tinta,
)

/** Cuatro jugadores repartidos; Caro (de turno, semilla 7) cae en Carrera Séptima, que es del Banco. */
private fun partidaFalsa(config: GameConfig): GameState {
    val s = withSampleProperties(Engine.newGame(config, listOf("Ana", "Beto", "Caro", "Dani"), seed = 7).state)
    val donde = listOf(11, 0, 24, 6)
    val plata = listOf(1240, 860, 1500, 315)
    return s.copy(players = s.players.mapIndexed { k, p -> p.copy(position = donde[k], money = plata[k]) })
}

@Composable
private fun MaquetaPartida(e: Estilo, config: GameConfig, state: GameState) {
    Column(Modifier.fillMaxSize().background(e.pantalla).safeDrawingPadding()) {
        if (e.franjasPantalla) FranjaChiva(12.dp)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(4.dp)) {
            val n = config.squares.size
            val grid = remember(n, maxWidth, maxHeight) { RingGrid.fit(n, maxWidth.value, maxHeight.value) }
            val cw = maxWidth / grid.cols
            val ch = maxHeight / grid.rows
            for (i in 0 until n) {
                val at = grid.cellOf(i)
                Casilla(e, config, state, i, cw, Modifier.offset(cw * at.col, ch * at.row).size(cw, ch))
            }
            Box(Modifier.offset(cw, ch).size(cw * (grid.cols - 2), ch * (grid.rows - 2)).padding(8.dp)) {
                Centro(e, state)
            }
        }
        if (e.franjasPantalla) FranjaChiva(12.dp)
    }
}

@Composable
private fun Casilla(e: Estilo, config: GameConfig, state: GameState, i: Int, ancho: Dp, modifier: Modifier) {
    val sq = config.squares[i]
    val holding = state.holdings[i]
    val ahora = state.players[state.current].position == i
    val letra = (ancho.value / 7f).coerceIn(7f, 12f).sp
    Column(
        modifier.background(if (ahora) Color(0xFFFFE08A) else e.casilla)
            .border(if (ahora) e.contorno * 2 else e.contorno, Tinta),
    ) {
        grupo(config, i)?.let { Box(Modifier.fillMaxWidth().height(ancho / 5).background(it).border(e.contorno / 2, Tinta)) }
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        ) {
            val icon = sq.icon()
            if (icon != null) IconImage(icon, minOf(ancho * 0.5f, 32.dp))
            else Text(
                sq.name, fontSize = letra, lineHeight = letra * 1.1f, maxLines = 2, overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center, fontFamily = e.texto, color = Tinta,
            )
            if (sq is OwnableSquare && holding == null) {
                Text(stringResource(R.string.money, sq.price), fontSize = letra, fontWeight = FontWeight.Bold, fontFamily = e.texto)
            }
        }
        val fichas = state.players.indices.filter { state.players[it].position == i }
        if (fichas.isNotEmpty()) Row(Modifier.fillMaxWidth().padding(bottom = 2.dp), horizontalArrangement = Arrangement.Center) {
            fichas.forEach { Ficha(PlayerColors[it], minOf(12.dp, (ancho - 6.dp) / 6)) }
        }
        holding?.let { Box(Modifier.fillMaxWidth().height(4.dp).background(PlayerColors[it.owner])) }
    }
}

@Composable
private fun Centro(e: Estilo, state: GameState) {
    val p = state.players[state.current]
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
        Calcomania(e) {
            if (!e.franjasPantalla) FranjaChiva(8.dp)
            Text(
                stringResource(R.string.turn_of, p.name), fontFamily = e.titulo, fontSize = 22.sp, color = Tinta,
                fontWeight = if (e.titulo == FontFamily.Default) FontWeight.Black else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp), textAlign = TextAlign.Center,
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconImage(Icon.dado(5), 48.dp)
                IconImage(Icon.dado(6), 48.dp)
                Text("= 11", fontSize = 24.sp, fontFamily = e.titulo, color = Tinta)
            }
            Column(Modifier.padding(horizontal = 10.dp).padding(bottom = 8.dp)) {
                state.players.forEachIndexed { k, j ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
                        Ficha(PlayerColors[k], 14.dp)
                        Spacer(Modifier.width(6.dp))
                        if (k == state.current) IconImage(Icon.TURNO, 14.dp, Modifier.padding(end = 3.dp))
                        Text(
                            j.name, Modifier.weight(1f), fontFamily = e.texto, fontSize = 15.sp, color = Tinta,
                            fontWeight = if (k == state.current) FontWeight.Bold else FontWeight.Normal,
                        )
                        Text(stringResource(R.string.money, j.money), fontFamily = e.texto, fontSize = 15.sp, color = Tinta)
                    }
                }
            }
        }
        Boton(e, stringResource(R.string.my_properties), principal = false, icono = Icon.CASA)
        Boton(e, stringResource(R.string.roll), principal = true)
    }
}

/** Panel con contorno; en la A, con sombra negra sólida desplazada (como una calcomanía). */
@Composable
private fun Calcomania(e: Estilo, contenido: @Composable () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Box(Modifier.fillMaxWidth()) {
        if (e.sombra) Box(Modifier.matchParentSize().offset(4.dp, 4.dp).background(Tinta, forma))
        Column(
            Modifier.fillMaxWidth().background(e.panel, forma).border(if (e.sombra) 2.5.dp else 2.dp, e.marco, forma)
                .padding(2.dp),
        ) { contenido() }
    }
}

@Composable
private fun Boton(e: Estilo, texto: String, principal: Boolean, icono: Icon? = null, modifier: Modifier = Modifier.fillMaxWidth()) {
    val fondo = if (principal) e.boton else e.secundario
    val tinta = if (principal) e.botonTexto else Tinta
    Box(modifier) {
        if (e.sombra) Box(Modifier.matchParentSize().offset(3.dp, 3.dp).background(Tinta, e.forma))
        Row(
            Modifier.fillMaxWidth().background(fondo, e.forma).border(2.dp, Tinta, e.forma).padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
        ) {
            icono?.let { IconImage(it, 20.dp); Spacer(Modifier.width(6.dp)) }
            Text(
                texto, color = tinta, fontFamily = e.titulo, fontSize = 16.sp, maxLines = 1, softWrap = false,
                fontWeight = if (e.titulo == FontFamily.Default) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

/** El diálogo de compra (`BuyDialog`) con el estilo: franja, banda del grupo, cifras y dos botones. */
@Composable
private fun DialogoCompra(e: Estilo, config: GameConfig, state: GameState) {
    val p = state.players[state.current]
    val i = p.position
    val sq = config.squares[i] as Property
    val forma = RoundedCornerShape(16.dp)
    Dialog(onDismissRequest = {}) {
        Box(Modifier.fillMaxWidth()) {
            if (e.sombra) Box(Modifier.matchParentSize().offset(5.dp, 5.dp).background(Tinta, forma))
            Column(Modifier.fillMaxWidth().background(e.panel, forma).border(3.dp, e.marco, forma).padding(3.dp)) {
                FranjaChiva(10.dp)
                Text(
                    stringResource(R.string.buy_title, p.name, sq.name), fontFamily = e.titulo, fontSize = 20.sp, color = Tinta,
                    fontWeight = if (e.titulo == FontFamily.Default) FontWeight.Black else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth().padding(10.dp), textAlign = TextAlign.Center,
                )
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp).background(grupo(config, i) ?: Tinta).border(1.5.dp, Tinta).padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) { Text(sq.name.uppercase(), color = Color.White, fontFamily = e.titulo, fontSize = 18.sp, fontWeight = FontWeight.Black) }
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Fila(e, "Alquiler", sq.rents.first())
                    Fila(e, "Con 1 casa", sq.rents[1])
                    Fila(e, "Con hotel", sq.rents.last())
                    Fila(e, "Te quedan", p.money - sq.price)
                }
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Boton(e, stringResource(R.string.decline), principal = false, modifier = Modifier.weight(1f))
                    Boton(e, "Comprar −$${sq.price}", principal = true, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Fila(e: Estilo, nombre: String, cifra: Int) {
    Row {
        Text(nombre, Modifier.weight(1f), fontFamily = e.texto, fontSize = 15.sp, color = Tinta)
        Text(stringResource(R.string.money, cifra), fontFamily = e.texto, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Tinta)
    }
}

/** Franja de chiva: dientes de colores sobre negro (D-28). */
@Composable
private fun FranjaChiva(alto: Dp) {
    Row(Modifier.fillMaxWidth().height(alto).background(Tinta)) {
        repeat(24) { Box(Modifier.weight(1f).fillMaxSize().padding(horizontal = 1.dp, vertical = alto / 4).background(Franja[it % Franja.size])) }
    }
}

@Composable
private fun Ficha(color: Color, size: Dp) {
    Box(Modifier.padding(horizontal = 1.dp).size(size).background(color, CircleShape).border(1.dp, Tinta, CircleShape))
}

private fun grupo(config: GameConfig, i: Int): Color? =
    (config.squares[i] as? Property)?.let { p -> config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) }
