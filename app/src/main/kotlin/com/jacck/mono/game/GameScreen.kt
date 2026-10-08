package com.jacck.mono.game

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import android.util.Log
import com.jacck.mono.LOG_TAG
import androidx.compose.runtime.setValue
import androidx.compose.ui.zIndex
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
import com.jacck.mono.DialogoChiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.board.Board
import com.jacck.mono.board.Hop
import com.jacck.mono.board.Fly
import com.jacck.mono.board.Bills
import androidx.compose.ui.layout.LayoutCoordinates
import com.jacck.mono.board.BuildingIcons
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.PlayersRow
import com.jacck.mono.board.SquareFace
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.engine.Dice
import com.jacck.mono.engine.Event
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.mortgageValue
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.model.Utility

/**
 * La partida en el tablero (F3.3, D-22): bajo los íconos, el letrero del turno con lo que toca y sus
 * botones (FD.4, D-69, D-70); en el centro, los dados y lo que hace falta para decidir (la escritura,
 * la subasta, lo que hizo la máquina). Arriba, los
 * íconos de los jugadores con su dinero ([hideMoney] lo abre oculto, extra `oculto`, D-66).
 * Tocar una casilla o un ícono pone su detalle en la ventana (FD.6, D-72): la carta de la casilla, o
 * el jugador con sus propiedades (marcadas en el tablero; las propias, en su turno, con sus jugadas);
 * deslizarlo hacia arriba lo saca volando. El final de la partida también va en la ventana. Solo
 * «¿Volver al menú?» ([onMenu], con «atrás», D-63) y el «¿Seguro?» de la quiebra van encima (D-67).
 */
@Composable
fun GameScreen(
    vm: GameViewModel, openProperties: Boolean = false, openSquare: Int? = null, askBankruptcy: Boolean = false,
    openCard: Int? = null, onMenu: (() -> Unit)? = null, openMenu: Boolean = false, hideMoney: Boolean = false, newSeed: () -> Long,
) {
    val state = vm.state
    // Lo tocado en la ventana (null, nada). Los extras `casilla`, `tarjeta` y `hoja` lo abren (sin `tarjeta`, quien juega).
    var detail by remember {
        mutableStateOf(
            openSquare?.takeIf { it in vm.config.squares.indices }?.let { Shown.Square(it) }
                ?: (openCard?.takeIf { it in state.players.indices } ?: state.current.takeIf { openProperties })?.let { Shown.Player(it) },
        )
    }
    val cardOf = (detail as? Shown.Player)?.player
    var showMoney by rememberSaveable { mutableStateOf(!hideMoney) }
    var askMenu by remember { mutableStateOf(openMenu) }
    BackHandler(enabled = onMenu != null) { askMenu = true }
    // El «¿Seguro?» de la quiebra en una deuda: es del teléfono, va en ventana (D-67); `seguro` lo abre.
    var confirmDebt by remember { mutableStateOf(askBankruptcy && state.phase is TurnPhase.Debt) }
    // «Declararme en quiebra» desde el jugador en la ventana; `hoja` y `seguro` lo abren.
    var confirmMe by remember { mutableStateOf(askBankruptcy && openProperties && state.phase !is TurnPhase.Debt) }
    val localTurn = vm.waitingFor == null && vm.machineTurn == null
    // La ficha camina con un saltito por casilla (FC.1, D-59): `steps` va de 0 al largo del recorrido.
    // La casilla comprada vuela al ícono (FD.1, D-65) y los billetes de un pago van de un ícono a otro
    // (FD.2, D-66): `steps` va de 0 a 1. `playing` dice a cuál ya se le puso `steps` en 0.
    val motion = vm.moving.firstOrNull()
    val walk = motion as? Walk
    val steps = remember { Animatable(0f) }
    var playing by remember { mutableStateOf(-1) }
    LaunchedEffect(vm.moved, motion != null) {
        val t0 = System.nanoTime()
        steps.snapTo(0f)
        playing = vm.moved
        when (val m = motion ?: return@LaunchedEffect) {
            is Walk -> {
                val n = m.path.size - 1
                steps.animateTo(n.toFloat(), tween(m.hopMs * n, easing = LinearEasing))
                val ms = (System.nanoTime() - t0) / 1_000_000
                Log.i(LOG_TAG, "recorrido de ${state.players[m.player].name}: ${m.path.first()} → ${m.path.last()}, $n saltos en $ms ms (${ms / n} ms por salto)")
            }
            is Flight -> {
                steps.animateTo(1f, tween(TINT_MS + FLY_MS, easing = LinearEasing))
                val ms = (System.nanoTime() - t0) / 1_000_000
                Log.i(LOG_TAG, "vuelo de ${vm.config.squares[m.square].name} a ${state.players[m.player].name}: $ms ms")
            }
            is Payment -> {
                steps.animateTo(1f, tween(PAY_MS, easing = LinearEasing))
                val ms = (System.nanoTime() - t0) / 1_000_000
                fun name(k: Int) = if (k == BANK) "Banco" else state.players[k].name
                val what = m.transfers.joinToString { "${name(it.from)} → ${name(it.to)} $${it.amount}" }
                Log.i(LOG_TAG, "pago $what: $ms ms")
            }
        }
        vm.motionDone()
    }
    val icons = remember { HashMap<Int, LayoutCoordinates>() }
    // Quien camina va en el aire; quien tiene un recorrido en cola espera donde empieza.
    val hops = buildMap {
        vm.moving.filterIsInstance<Walk>().asReversed().forEach { put(it.player, Hop(it.path.first(), it.path.first())) }
        if (walk != null) {
            val i = steps.value.toInt().coerceIn(0, walk.path.size - 2)
            put(walk.player, Hop(walk.path[i], walk.path[i + 1], (steps.value - i).coerceIn(0f, 1f)))
        }
    }
    // Hasta que su animación arranca, el pago va en 0 (no en el 1 del anterior).
    val pay = motion as? Payment
    val f = if (playing == vm.moved) steps.value.coerceIn(0f, 1f) else 0f
    PantallaChiva {
        Board(
            vm.config, state, Modifier.fillMaxSize().padding(4.dp), onSquare = { detail = tapped(detail, Shown.Square(it)) },
            highlight = if (walk == null) state.players[state.current].position else null, hops = hops,
            marks = cardOf?.let { k -> state.holdings.filterValues { it.owner == k }.keys }.orEmpty(),
            markColor = cardOf?.let { PlayerColors[it] } ?: Color.Unspecified,
            flight = (motion as? Flight)?.let { Fly(it.square, it.player, steps.value.coerceIn(0f, 1f), TINT_MS / (TINT_MS + FLY_MS).toFloat()) },
            bills = pay?.let { p -> Bills(p.transfers.map { it.from to it.to }, f) },
            iconOf = { icons[it] },
        ) {
            Column(Modifier.fillMaxSize()) {
                PlayersRow(
                    state, showMoney, Modifier.zIndex(1f), money = shownMoney(state.players.map { it.money }, vm.moving, f),
                    swing = pay?.deltas.orEmpty(), swingF = f,
                    onTap = { detail = tapped(detail, Shown.Player(it)) }, onLongPress = { showMoney = !showMoney },
                    onPlaced = { k, c -> icons[k] = c },
                )
                turnBar(vm.config, state, vm.bots, vm.remote?.seats.orEmpty(), vm.held)?.let { bar ->
                    TurnSign(vm, bar, enabled = motion == null) { b ->
                        when {
                            b.action != null -> vm.act(b.action)
                            b.kind == BarKind.NEXT -> vm.next()
                            b.kind == BarKind.SELL_OR_MORTGAGE -> detail = Shown.Player(decider(state))
                            b.kind == BarKind.BANKRUPTCY -> confirmDebt = true
                        }
                    }
                }
                Box(Modifier.weight(1f).padding(top = 10.dp), contentAlignment = Alignment.Center) {
                    val phase = state.phase
                    if (phase is TurnPhase.Over) {
                        OverWindow(vm, phase.winners, onMenu?.let { { vm.leave(); it() } }) { detail = null; vm.restart(newSeed()) }
                    } else Window(vm)
                    when (val top = shown(detail, phase)) {
                        is Shown.Square, is Shown.Player -> DetailWindow(
                            vm, top, manage = top is Shown.Player && canManage(state, top.player, localTurn), onBankrupt = { confirmMe = true },
                        ) { detail = null }
                        else -> Unit
                    }
                }
            }
        }
    }
    if (askMenu && onMenu != null) {
        ConfirmMenu(linked = vm.remote != null, onCancel = { askMenu = false }) { vm.leave(); onMenu() }
        return
    }
    if (motion != null) return // el «¿Seguro?» sale cuando la ficha llega y la casilla vuela
    if (confirmMe && cardOf != null && canManage(state, cardOf, localTurn)) {
        ConfirmBankruptcy(vm, cardOf, null, onCancel = { confirmMe = false }) { confirmMe = false }
        return
    }
    // Las decisiones del turno van en el letrero (FD.4) y el final, en la ventana (FD.6).
    when (val phase = state.phase) {
        is TurnPhase.Debt -> if (confirmDebt && localTurn) {
            ConfirmBankruptcy(vm, phase.debts.first().debtor, phase.debts.first().creditor, onCancel = { confirmDebt = false }) { confirmDebt = false }
        }
        else -> Unit
    }
}

/**
 * El letrero del turno (FD.4, D-69): una línea con lo que toca y debajo sus botones, en una fila;
 * los de más de 7 letras pesan 2 y las cifras, «Pasar» y «Quebrar», 1. Mientras algo se anima, apagados.
 */
@Composable
private fun TurnSign(vm: GameViewModel, bar: TurnBar, enabled: Boolean, onButton: (BarButton) -> Unit) {
    val players = vm.state.players
    fun sq(i: Int) = vm.config.squares[i].name
    val t = bar.title
    val title = when (t) {
        is BarTitle.Turn -> stringResource(R.string.bar_turn, players[t.player].name)
        is BarTitle.Buy -> stringResource(R.string.bar_buy, sq(t.square))
        is BarTitle.Auction -> stringResource(R.string.bar_auction, sq(t.square), players[t.bidder].name)
        is BarTitle.Tax -> stringResource(R.string.bar_tax, sq(t.square))
        is BarTitle.Jail -> stringResource(R.string.bar_jail, players[t.player].name, t.turn, t.of)
        is BarTitle.Debt -> stringResource(R.string.debt_title, players[t.player].name, money(players[t.player].money))
        is BarTitle.Machine -> stringResource(R.string.machine_playing, players[t.player].name)
        is BarTitle.MachinePlayed -> stringResource(R.string.bar_machine_played, players[t.player].name)
        is BarTitle.Waiting -> stringResource(R.string.waiting_remote, players[t.player].name)
    }
    val icon = when (t) {
        is BarTitle.Machine, is BarTitle.MachinePlayed -> Icon.MAQUINA
        is BarTitle.Waiting -> Icon.ENLACE
        else -> null
    }
    // Arriba, el hueco del «+$x/−$x» que cuelga bajo el dinero en un pago (15 sp, D-66): sin él tapa el título.
    Calcomania(Modifier.fillMaxWidth().padding(top = 26.dp)) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let { IconImage(it, 22.dp); Spacer(Modifier.width(6.dp)) }
                Text(title, maxLines = 1, softWrap = false, autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 15.sp))
            }
            if (bar.buttons.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    bar.buttons.forEach { b ->
                        val label = buttonLabel(b)
                        BotonChiva(
                            label, { onButton(b) }, Modifier.weight(if (label.length > 7) 2f else 1f), principal = b.principal,
                            enabled = enabled && b.enabled,
                        )
                    }
                }
            }
        }
    }
}

/** El texto de un botón del letrero; con dinero, verbo y cifra con su signo (M-026), salvo las pujas (D-69). */
@Composable
private fun buttonLabel(b: BarButton): String {
    val amount = b.amount ?: 0
    return when (b.kind) {
        BarKind.ROLL -> stringResource(R.string.roll)
        BarKind.END_TURN -> stringResource(R.string.end_turn)
        BarKind.BUY -> stringResource(R.string.buy, money(amount))
        BarKind.DECLINE -> stringResource(R.string.decline)
        BarKind.BID -> money(amount)
        BarKind.PASS -> stringResource(R.string.pass_bid)
        BarKind.TAX_FIXED -> stringResource(R.string.bar_tax_fixed, money(amount))
        BarKind.TAX_PERCENT -> stringResource(R.string.bar_tax_percent, amount)
        BarKind.JAIL_ROLL -> stringResource(R.string.bar_jail_roll)
        BarKind.JAIL_FINE -> stringResource(R.string.bar_jail_fine, money(amount))
        BarKind.JAIL_CARD -> stringResource(R.string.bar_jail_card)
        BarKind.SELL_OR_MORTGAGE -> stringResource(R.string.bar_sell)
        BarKind.BANKRUPTCY -> stringResource(R.string.bar_bankrupt)
        BarKind.NEXT -> stringResource(R.string.next)
    }
}

/**
 * La ventana «Lo que pasa» (FD.5, D-71): una tarjeta por turno, la de ahora arriba y entera, la
 * anterior debajo y atenuada; en la de ahora, lo que decían los diálogos del turno (la subasta, la
 * deuda) y el último rechazo. Al comprar, la escritura en lugar de la tarjeta anterior.
 */
@Composable
private fun Window(vm: GameViewModel) {
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

/** «¿Volver al menú?» con «atrás» (D-63): la local queda guardada; la enlazada corta la conexión. */
@Composable
private fun ConfirmMenu(linked: Boolean, onCancel: () -> Unit, onYes: () -> Unit) {
    DialogoChiva(
        stringResource(R.string.to_menu_sure), color = Chiva.Azul,
        botones = {
            BotonChiva(stringResource(R.string.to_menu_yes), onYes)
            BotonChiva(stringResource(R.string.to_menu_no), onCancel, principal = false)
        },
    ) {
        Text(stringResource(if (linked) R.string.to_menu_link else R.string.to_menu_saved), fontSize = 16.sp)
    }
}

/** «¿Seguro?» antes de quebrar (D-56): qué pierde y a quién va lo suyo; con «Sí», `onDone` y la quiebra. */
@Composable
private fun ConfirmBankruptcy(vm: GameViewModel, player: Int, creditor: Int?, onCancel: () -> Unit, onDone: () -> Unit = {}) {
    val players = vm.state.players
    DialogoChiva(
        stringResource(R.string.bankruptcy_sure, players[player].name), color = Chiva.Techo,
        botones = {
            BotonChiva(stringResource(R.string.bankruptcy_yes), { onDone(); vm.act(Action.DeclareBankruptcy) })
            BotonChiva(stringResource(R.string.bankruptcy_no), onCancel, principal = false)
        },
    ) {
        Text(
            if (creditor != null) stringResource(R.string.bankruptcy_to_player, players[creditor].name)
            else stringResource(R.string.bankruptcy_to_bank),
            fontSize = 16.sp,
        )
    }
}

/** El final de la partida en la ventana (FD.6, D-67): quién gana, otra partida o el menú, y el último turno. */
@Composable
private fun OverWindow(vm: GameViewModel, winners: List<Int>, onMenu: (() -> Unit)?, onRestart: () -> Unit) {
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
private fun DetailWindow(vm: GameViewModel, detail: Shown, manage: Boolean, onBankrupt: () -> Unit, onGone: () -> Unit) {
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
