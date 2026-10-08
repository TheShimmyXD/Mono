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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.jacck.mono.board.SquareCard
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.engine.Dice
import com.jacck.mono.engine.minimumBid
import com.jacck.mono.engine.mortgageValue
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.model.Utility

/**
 * La partida en el tablero (F3.3, D-22): bajo los íconos, el letrero del turno con lo que toca y sus
 * botones (FD.4, D-69, D-70); en el centro, los dados y lo que hace falta para decidir (la escritura,
 * la subasta, lo que hizo la máquina). Arriba, los
 * íconos de los jugadores con su dinero ([hideMoney] lo abre oculto, extra `oculto`, D-66); un toque abre
 * su tarjeta y marca sus casillas (FC.3, D-60).
 * Las propiedades de cada uno van en una hoja que sube desde abajo (F3.4, D-24): la propia, en su
 * turno, con sus jugadas; la de otro, solo para mirar. Con [onMenu], «atrás» pregunta si se vuelve al
 * menú y el final de la partida lo ofrece (D-63).
 */
@Composable
fun GameScreen(
    vm: GameViewModel, openProperties: Boolean = false, openSquare: Int? = null, askBankruptcy: Boolean = false,
    openCard: Int? = null, onMenu: (() -> Unit)? = null, openMenu: Boolean = false, hideMoney: Boolean = false, newSeed: () -> Long,
) {
    val state = vm.state
    // Hoja abierta: la de quién (null, ninguna). La del extra `hoja` es la de `tarjeta` o, sin ella, la de quien juega.
    var sheetOf by remember { mutableStateOf(if (openProperties) openCard?.takeIf { it in state.players.indices } ?: state.current else null) }
    var cardOf by rememberSaveable { mutableStateOf(openCard?.takeIf { it in state.players.indices }) }
    var showMoney by rememberSaveable { mutableStateOf(!hideMoney) }
    var askMenu by remember { mutableStateOf(openMenu) }
    BackHandler(enabled = onMenu != null) { askMenu = true }
    var shownSquare by remember { mutableStateOf(openSquare) }
    // El «¿Seguro?» de la quiebra en una deuda: es del teléfono, va en ventana (D-67); `seguro` lo abre.
    var confirmDebt by remember { mutableStateOf(askBankruptcy && state.phase is TurnPhase.Debt) }
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
            vm.config, state, Modifier.fillMaxSize().padding(4.dp), onSquare = { shownSquare = it },
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
                    onTap = { cardOf = if (cardOf == it) null else it }, onLongPress = { showMoney = !showMoney },
                    onPlaced = { k, c -> icons[k] = c },
                )
                turnBar(vm.config, state, vm.bots, vm.remote?.seats.orEmpty(), vm.held)?.let { bar ->
                    TurnSign(vm, bar, enabled = motion == null) { b ->
                        when {
                            b.action != null -> vm.act(b.action)
                            b.kind == BarKind.NEXT -> vm.next()
                            b.kind == BarKind.SELL_OR_MORTGAGE -> sheetOf = decider(state)
                            b.kind == BarKind.BANKRUPTCY -> confirmDebt = true
                        }
                    }
                }
                val k = cardOf
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (k != null) PlayerCard(state, k, onProperties = { sheetOf = k }) { cardOf = null } else Center(vm)
                }
            }
        }
    }
    if (askMenu && onMenu != null) {
        ConfirmMenu(linked = vm.remote != null, onCancel = { askMenu = false }) { vm.leave(); onMenu() }
        return
    }
    if (motion != null) return // lo que pasó y la decisión salen cuando la ficha llega y la casilla vuela
    // La carta de una casilla (FA.3) va primero: los diálogos del turno vuelven al cerrarla.
    shownSquare?.let {
        SquareCard(vm.config, state, it) { shownSquare = null }
        return
    }
    sheetOf?.let { k ->
        PropertiesSheet(vm, k, canManage(state, k, vm.waitingFor == null && vm.machineTurn == null), askBankruptcy) { sheetOf = null }
        return
    }
    val lines = vm.notices.mapNotNull { eventLine(it, vm.config, state) }
    if (lines.isNotEmpty()) {
        NoticesDialog(lines, vm::dismissNotices)
        return
    }
    // Las decisiones del turno van en el letrero (FD.4); el final de la partida, hasta FD.6, en ventana.
    when (val phase = state.phase) {
        is TurnPhase.Over -> OverDialog(vm, phase.winners, onMenu?.let { { vm.leave(); it() } }) { vm.restart(newSeed()) }
        is TurnPhase.Debt -> if (confirmDebt && vm.waitingFor == null && vm.machineTurn == null) {
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
 * El centro del tablero: los dados, lo que hizo la máquina en su última jugada (FD.4, D-70) y lo que
 * decían los diálogos del turno (la subasta, la deuda); al comprar, la escritura. Hasta FD.5.
 */
@Composable
private fun Center(vm: GameViewModel) {
    val state = vm.state
    val players = state.players
    val phase = state.phase
    val lines = vm.said.mapNotNull { eventLine(it, vm.config, state) }.toMutableList()
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
    Column(Modifier.fillMaxSize().padding(2.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        val dice = vm.lastDice
        if (dice != null || lines.isNotEmpty() || vm.error != null) {
            Calcomania {
                dice?.let { DiceRow(it) }
                Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    lines.forEach { Text(it, fontSize = 15.sp) }
                    vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                }
            }
        }
        if (phase is TurnPhase.Buy) Deed(vm.config, state, phase.square)
    }
}

@Composable
private fun DiceRow(dice: Dice) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconImage(Icon.dado(dice.first), 48.dp)
        IconImage(Icon.dado(dice.second), 48.dp)
        Text("= ${dice.total}", fontSize = 24.sp)
    }
}

/**
 * Hoja de propiedades de [owner] (F3.4, D-24; FC.3, D-60): sus casillas en orden del anillo, con su
 * franja y sus edificios. Si [manage], un botón por cada jugada que el motor acepta ahora
 * (`propertyMoves`) y «Declararme en quiebra» (D-56); si no, solo se mira.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertiesSheet(vm: GameViewModel, owner: Int, manage: Boolean, ask: Boolean, onClose: () -> Unit) {
    val state = vm.state
    val mine = state.holdings.filter { it.value.owner == owner }.toSortedMap()
    var confirm by remember { mutableStateOf(ask && manage) }
    if (confirm) {
        ConfirmBankruptcy(vm, owner, null, onCancel = { confirm = false }) { onClose() }
        return
    }
    ModalBottomSheet(
        onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Chiva.Sol,
    ) {
        Column(
            Modifier.padding(start = 14.dp, end = 17.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Calcomania {
                Row(Modifier.fillMaxWidth().background(Chiva.Techo).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    PlayerToken(state, owner, 30.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.properties_title, state.players[owner].name), color = Color.White, fontSize = 20.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(money(state.players[owner].money), color = Color.White, fontSize = 20.sp)
                }
            }
            if (manage) Rejected(vm)
            if (mine.isEmpty()) Text(stringResource(R.string.properties_none), fontSize = 16.sp)
            mine.forEach { (square, holding) -> PropertyRow(vm, square, holding, manage) }
            if (manage) BotonChiva(stringResource(R.string.bankruptcy_me), { confirm = true }, principal = false)
        }
    }
}

/** Una propiedad en su tarjeta: franja del grupo, nombre, estado y edificios; debajo, sus jugadas. */
@Composable
private fun PropertyRow(vm: GameViewModel, square: Int, holding: Holding, manage: Boolean) {
    val sq = vm.config.squares[square]
    val band = (sq as? Property)?.let { p -> vm.config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Color.LightGray
    val status = when {
        holding.mortgaged -> stringResource(R.string.mortgaged_short)
        holding.hotel || holding.houses > 0 -> null
        sq is Property -> stringResource(R.string.no_houses)
        else -> null
    }
    val moves = if (manage) propertyMoves(vm.config, vm.state, square) else emptyList()
    Calcomania(sombra = 3.dp, borde = 2.dp, forma = RoundedCornerShape(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp, 44.dp).background(band))
            Column(Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(sq.name, fontSize = 16.sp)
                status?.let { Text(it, fontSize = 13.sp) }
                if (!holding.mortgaged) BuildingIcons(holding, 16.dp)
            }
        }
        if (moves.isNotEmpty()) {
            Row(Modifier.padding(start = 8.dp, end = 11.dp, bottom = 11.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                moves.forEach { move ->
                    val amount = money(move.amount)
                    val (text, main) = when (move.action) {
                        is Action.Build -> stringResource(if (move.hotel) R.string.move_hotel else R.string.move_house, amount) to true
                        is Action.SellBuilding -> stringResource(R.string.move_sell, amount) to false
                        is Action.Mortgage -> stringResource(R.string.move_mortgage, amount) to false
                        is Action.Unmortgage -> stringResource(R.string.move_unmortgage, amount) to true
                        else -> null to false
                    }
                    if (text != null) BotonChiva(text, { vm.act(move.action) }, Modifier.weight(1f), principal = main)
                }
            }
        }
    }
}

/** Motivo del último rechazo del motor, en rojo, dentro de un diálogo. */
@Composable
private fun Rejected(vm: GameViewModel) {
    vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
}

@Composable
private fun NoticesDialog(lines: List<String>, onDone: () -> Unit) {
    DialogoChiva(
        stringResource(R.string.what_happened), color = Chiva.Azul,
        botones = { BotonChiva(stringResource(R.string.next), onDone) },
    ) {
        lines.forEach { Text("• $it", fontSize = 16.sp) }
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

@Composable
private fun OverDialog(vm: GameViewModel, winners: List<Int>, onMenu: (() -> Unit)?, onRestart: () -> Unit) {
    DialogoChiva(
        stringResource(R.string.game_over), color = Chiva.Verde,
        botones = {
            if (vm.remote == null) BotonChiva(stringResource(R.string.new_game), onRestart)
            if (onMenu != null) BotonChiva(stringResource(R.string.to_menu), onMenu, principal = vm.remote != null)
        },
    ) {
        Text(
            stringResource(R.string.winners, winners.joinToString(" y ") { vm.state.players[it].name }), fontSize = 20.sp,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
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
                else -> Unit
            }
            runCatching { mortgageValue(config, state, square) }.getOrNull()?.let {
                Text(stringResource(R.string.deed_mortgage, money(it)), fontSize = 13.sp)
            }
        }
    }
}
