package com.jacck.mono.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.personaje
import com.jacck.mono.engine.model.EndCondition
import com.jacck.mono.engine.model.GameRecord
import com.jacck.mono.engine.model.RecordPlayer

/** Cromo dorado: tonos de oro con un brillo claro en medio, en espejo para que el barrido no tenga corte (D-92). */
private val Oro = listOf(Color(0xFFA67C00), Color(0xFFE8B923), Color(0xFFFFF4C2), Color(0xFFE8B923), Color(0xFFA67C00))

/** Aro de quien no ganó, y de todos en una partida sin terminar (D-92). */
private val Gris = Color(0xFF9E9E9E)

/** Lo que tarda el brillo del cromo en cruzar el medallón. */
const val CHROME_MS = 2200

/**
 * El historial de partidas (FH.2, D-92): una tarjeta por partida, la más nueva arriba, con fecha,
 * tablero, tiempo, turnos y los jugadores; solo quien ganó va a color, con cromo dorado detrás.
 */
@Composable
fun HistoryScreen(records: List<GameRecord>, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    PantallaChiva {
        Text(stringResource(R.string.hist_title), fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (records.isEmpty()) item { Text(stringResource(R.string.hist_empty), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
            items(records.asReversed(), key = { it.started }) { Tarjeta(it) }
        }
        BotonChiva(stringResource(R.string.to_menu), onBack, Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), principal = false)
    }
}

@Composable
private fun Tarjeta(r: GameRecord) = Calcomania {
    val days = stringArrayResource(R.array.hist_days)
    val months = stringArrayResource(R.array.hist_months)
    val (day, date, month) = dayMonth(r.started)
    val (h, m) = hoursMinutes(r.playedMs)
    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.hist_date, days[day], date, months[month]) + " · " + r.board, fontSize = 15.sp, maxLines = 1, modifier = Modifier.weight(1f))
            Text(
                (if (h > 0) stringResource(R.string.hist_hours, h, m) else stringResource(R.string.hist_minutes, m)) + " · " +
                    pluralStringResource(R.plurals.hist_turns, r.turns, r.turns),
                fontSize = 13.sp, color = Chiva.Tinta.copy(alpha = 0.7f),
            )
        }
        // Cinco o seis jugadores no caben en una fila de 72 dp: se reparten el ancho.
        Row {
            r.players.forEachIndexed { k, p ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = if (r.players.size <= 4) Modifier.width(72.dp) else Modifier.weight(1f)) {
                    MedallonHistorial(p, k, r.winners?.contains(k) == true, 40.dp)
                    Text(p.name, fontSize = 12.sp, maxLines = 1)
                }
            }
        }
        when {
            r.winners == null -> stringResource(R.string.hist_unfinished)
            r.end == EndCondition.TIME_LIMIT -> stringResource(R.string.hist_end_time)
            r.end == EndCondition.SECOND_BANKRUPTCY -> stringResource(R.string.hist_end_second)
            else -> null
        }?.let { Text(it, fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.6f)) }
    }
}

/**
 * Medallón del historial: como `Medallon` (D-35), pero solo el ganador a color, con un cromo dorado que
 * barre detrás de su personaje; los demás, aro gris y personaje en blanco y negro. Arriba a la derecha,
 * como una notificación, el robot si lo jugó la máquina o el enlace si estaba en otro teléfono.
 */
@Composable
private fun MedallonHistorial(p: RecordPlayer, k: Int, won: Boolean, size: Dp) {
    val shine by rememberInfiniteTransition(label = "cromo").animateFloat(
        0f, 1f, infiniteRepeatable(tween(CHROME_MS, easing = LinearEasing)), label = "cromo",
    )
    Box(Modifier.size(size + 8.dp)) {
        Box(
            Modifier.align(Alignment.BottomStart).size(size).clip(CircleShape)
                .drawBehind {
                    if (won) {
                        val w = this.size.width
                        val x = -w + 2 * w * shine
                        drawRect(Brush.linearGradient(Oro, Offset(x, 0f), Offset(x + w, this.size.height), TileMode.Mirror))
                    } else drawRect(Color.White)
                }
                .border(size / 10, if (won) PlayerColors[k] else Gris, CircleShape).border(size / 35, Chiva.Tinta, CircleShape),
        ) {
            Image(
                painterResource(personaje(p.token, k)), null, Modifier.size(size).padding(size / 8),
                colorFilter = if (won) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
            )
        }
        val badge = if (p.machine) Icon.MAQUINA else if (p.remote) Icon.ENLACE else null
        badge?.let {
            Box(
                Modifier.align(Alignment.TopEnd).size(20.dp).clip(CircleShape).background(Color.White).border(1.5.dp, Chiva.Tinta, CircleShape),
                contentAlignment = Alignment.Center,
            ) { IconImage(it, 14.dp) }
        }
    }
}
