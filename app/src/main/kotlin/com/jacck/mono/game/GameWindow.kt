package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.animate
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.core.tween
import android.util.Log
import com.jacck.mono.LOG_TAG
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.R
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.SquareFace
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.engine.Dice
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.mortgageValue
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.model.Utility

/**
 * La ventana «Lo que pasa» (FD.5, D-71): una tarjeta por turno, la de ahora arriba y entera, la
 * anterior debajo y atenuada; en la de ahora, lo que decían los diálogos del turno (la subasta, la
 * deuda) y el último rechazo. Al comprar, la escritura en lugar de la tarjeta anterior.
 */
@Composable
internal fun Window(vm: GameViewModel) {
    val state = vm.state
    val players = state.players
    val phase = state.phase
    val lines = mutableListOf<String>()
    when (phase) {
        is TurnPhase.Auction -> {
            val leader = phase.highestBidder
            lines += if (leader == null) stringResource(R.string.auction_none, money(minimumBid(vm.config, phase.square)))
            else stringResource(R.string.auction_leader, players[leader].name, money(phase.highestBid))
            val bidder = nextBidder(phase)
            lines += stringResource(R.string.bar_auction_money, players[bidder].name, money(players[bidder].money))
        }
        is TurnPhase.Debt -> lines += stringResource(R.string.debt_text)
        else -> Unit
    }
    if (vm.waitingFor != null && vm.remote?.connected == false) lines += stringResource(R.string.bar_cut)
    val cards = vm.log.ifEmpty { listOf(TurnLog(state.current, emptyList())) }.asReversed()
    Calcomania(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.win_title), Modifier.fillMaxWidth().background(Chiva.Azul).padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White, fontSize = 15.sp,
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TurnCard(vm, cards.first(), faded = false) {
                lines.forEach { Text("• $it", fontSize = 14.sp) }
                vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
            if (phase is TurnPhase.Buy) Deed(vm.config, state, phase.square)
            else cards.getOrNull(1)?.let { TurnCard(vm, it, faded = true) }
        }
    }
}

/**
 * Una tarjeta de la ventana: medallón, nombre y los primeros dados del turno; debajo, lo que pasó en
 * orden (los dados de un tiro más tras dobles, en su sitio) y [more].
 */
@Composable
private fun TurnCard(vm: GameViewModel, turn: TurnLog, faded: Boolean, more: @Composable () -> Unit = {}) {
    val state = vm.state
    val first = turn.events.indexOfFirst { it is Event.DiceRolled }
    Calcomania(Modifier.fillMaxWidth().alpha(if (faded) 0.5f else 1f), sombra = 2.dp, borde = 1.5.dp) {
        Row(Modifier.fillMaxWidth().background(Chiva.Turno).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            PlayerToken(state, turn.player, 24.dp)
            Spacer(Modifier.width(6.dp))
            Text(state.players[turn.player].name, Modifier.weight(1f), fontSize = 15.sp, maxLines = 1)
            (turn.events.getOrNull(first) as? Event.DiceRolled)?.let { DiceIcons(it.dice, 24.dp) }
        }
        Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            turn.events.forEachIndexed { i, e ->
                if (e is Event.DiceRolled) {
                    if (i != first) DiceIcons(e.dice, 20.dp)
                } else {
                    eventLine(e, vm.config, state)?.let { Text("• $it", fontSize = 14.sp) }
                }
            }
            more()
        }
    }
}

@Composable
private fun DiceIcons(dice: Dice, size: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconImage(Icon.dado(dice.first), size)
        IconImage(Icon.dado(dice.second), size)
        Text("= ${dice.total}", fontSize = (size.value / 2).sp)
    }
}

/** El final de la partida en la ventana (FD.6, D-67): quién gana, otra partida o el menú, y el último turno. */
@Composable
internal fun OverWindow(vm: GameViewModel, winners: List<Int>, onMenu: (() -> Unit)?, onRestart: () -> Unit) {
    Calcomania(Modifier.fillMaxSize()) {
        Text(
            stringResource(R.string.game_over), Modifier.fillMaxWidth().background(Chiva.Verde).padding(horizontal = 10.dp, vertical = 4.dp),
            color = Color.White, fontSize = 15.sp,
        )
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(R.string.winners, winners.joinToString(" y ") { vm.state.players[it].name }), fontSize = 22.sp,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            if (vm.remote == null) BotonChiva(stringResource(R.string.new_game), onRestart)
            if (onMenu != null) BotonChiva(stringResource(R.string.to_menu), onMenu, principal = vm.remote != null)
            vm.log.lastOrNull()?.let { TurnCard(vm, it, faded = true) }
        }
    }
}

/**
 * Lo tocado en la ventana (FD.6, D-72, opción A): la franja azul dice qué es; debajo, la carta de la
 * casilla o el jugador. Subirlo con el dedo (desde la franja, o desde abajo del todo) lo saca volando
 * hacia arriba y deja ver lo que había debajo (`swipeCloses`); si no sube lo bastante, vuelve a su sitio.
 */
@Composable
internal fun DetailWindow(vm: GameViewModel, detail: Shown, manage: Boolean, onBankrupt: () -> Unit, onGone: () -> Unit) {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    var lift by remember(detail) { mutableFloatStateOf(0f) }
    var flying by remember(detail) { mutableStateOf(false) }
    var height by remember { mutableIntStateOf(0) }
    // Sube (o baja, sin pasar de su sitio) con el dedo; devuelve lo que usó.
    fun drag(dy: Float): Float {
        if (flying) return 0f
        val next = (lift + dy).coerceAtMost(0f)
        return (next - lift).also { lift = next }
    }
    fun release(speed: Float) {
        if (flying) return
        val out = swipeCloses(lift / density, speed / density)
        flying = out
        scope.launch {
            val t0 = System.nanoTime()
            animate(lift, if (out) -height * 1.2f else 0f, animationSpec = tween(if (out) SWIPE_MS else SWIPE_MS / 2)) { v, _ -> lift = v }
            if (out) {
                Log.i(LOG_TAG, "deslizar: $detail fuera en ${(System.nanoTime() - t0) / 1_000_000} ms")
                onGone()
            }
        }
    }
    val connection = remember(detail) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (lift < 0f && available.y > 0f) Offset(0f, drag(available.y)) else Offset.Zero

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (available.y < 0f && source == NestedScrollSource.UserInput) Offset(0f, drag(available.y)) else Offset.Zero

            override suspend fun onPreFling(available: Velocity): Velocity =
                if (lift < 0f) available.also { release(it.y) } else Velocity.Zero
        }
    }
    val title = when (detail) {
        is Shown.Player -> vm.state.players[detail.player].name
        else -> stringResource(R.string.win_square)
    }
    Calcomania(
        Modifier.fillMaxSize().onSizeChanged { height = it.height }.graphicsLayer {
            translationY = lift
            alpha = 1f - 0.6f * (-lift / height.coerceAtLeast(1)).coerceIn(0f, 1f)
        },
    ) {
        Column(Modifier.fillMaxWidth().draggable(rememberDraggableState { drag(it) }, Orientation.Vertical, onDragStopped = { release(it) })) {
            Text(title, Modifier.fillMaxWidth().background(Chiva.Azul).padding(horizontal = 10.dp, vertical = 4.dp), color = Color.White, fontSize = 15.sp, maxLines = 1)
            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(44.dp, 5.dp).clip(RoundedCornerShape(3.dp)).background(Color.Gray))
                Text(stringResource(R.string.win_swipe), fontSize = 11.sp, color = Color.Gray)
            }
        }
        Column(Modifier.fillMaxSize().nestedScroll(connection).verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            when (detail) {
                is Shown.Square -> SquareFace(vm.config, vm.state, detail.square)
                is Shown.Player -> PlayerDetail(vm, detail.player, manage, onBankrupt)
                else -> Unit
            }
        }
    }
}

/** Escritura de una casilla con dueño posible: franja del grupo, precio, alquileres e hipoteca. */
@Composable
private fun Deed(config: GameConfig, state: GameState, square: Int) {
    val sq = config.squares[square] as OwnableSquare
    val band = (sq as? Property)?.let { p -> config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Color.LightGray
    val forma = RoundedCornerShape(10.dp)
    Column(Modifier.fillMaxWidth().clip(forma).border(2.dp, Chiva.Tinta, forma)) {
        Box(Modifier.fillMaxWidth().height(36.dp).background(band), contentAlignment = Alignment.Center) {
            Text(sq.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        }
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.deed_price, money(sq.price)), fontWeight = FontWeight.Bold)
            when (sq) {
                is Property -> {
                    Text(stringResource(R.string.deed_rent, money(sq.rents[0])))
                    sq.rents.drop(1).dropLast(1).forEachIndexed { k, r ->
                        Text(pluralStringResource(R.plurals.deed_rent_houses, k + 1, k + 1, money(r)), fontSize = 13.sp)
                    }
                    if (sq.rents.size > 1) Text(stringResource(R.string.deed_rent_hotel, money(sq.rents.last())), fontSize = 13.sp)
                    val house = sq.housePrice ?: config.rules.housePrice
                    if (house != null) Text(stringResource(R.string.deed_house, money(house)), fontSize = 13.sp)
                }
                is Station -> sq.rents.forEachIndexed { k, r ->
                    Text(stringResource(R.string.deed_station_rent, k + 1, money(r)), fontSize = 13.sp)
                }
                is Utility -> sq.diceMultipliers.forEachIndexed { k, m ->
                    Text(stringResource(R.string.deed_utility, m, k + 1), fontSize = 13.sp)
                }
            }
            runCatching { mortgageValue(config, state, square) }.getOrNull()?.let {
                Text(stringResource(R.string.deed_mortgage, money(it)), fontSize = 13.sp)
            }
        }
    }
}
