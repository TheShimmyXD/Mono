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
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.TurnPhase
import com.jacck.mono.engine.model.Utility

/**
 * La partida en el tablero (F3.3, D-22): en el centro, de quién es el turno, los dados, los
 * jugadores y la acción principal; cada decisión del motor sale en un diálogo encima. Arriba, los
 * íconos de los jugadores con su dinero; un toque abre su tarjeta y marca sus casillas (FC.3, D-60).
 * Las propiedades de cada uno van en una hoja que sube desde abajo (F3.4, D-24): la propia, en su
 * turno, con sus jugadas; la de otro, solo para mirar. Con [onMenu], «atrás» pregunta si se vuelve al
 * menú y el final de la partida lo ofrece (D-63).
 */
@Composable
fun GameScreen(
    vm: GameViewModel, openProperties: Boolean = false, openSquare: Int? = null, askBankruptcy: Boolean = false,
    openCard: Int? = null, onMenu: (() -> Unit)? = null, openMenu: Boolean = false, newSeed: () -> Long,
) {
    val state = vm.state
    // Hoja abierta: la de quién (null, ninguna). La del extra `hoja` es la de `tarjeta` o, sin ella, la de quien juega.
    var sheetOf by remember { mutableStateOf(if (openProperties) openCard?.takeIf { it in state.players.indices } ?: state.current else null) }
    var cardOf by rememberSaveable { mutableStateOf(openCard?.takeIf { it in state.players.indices }) }
    var showMoney by rememberSaveable { mutableStateOf(true) }
    var askMenu by remember { mutableStateOf(openMenu) }
    BackHandler(enabled = onMenu != null) { askMenu = true }
    var shownSquare by remember { mutableStateOf(openSquare) }
    // La ficha camina con un saltito por casilla (FC.1, D-59): `steps` va de 0 al largo del recorrido.
    // La casilla comprada vuela al ícono (FD.1, D-65): `steps` va de 0 a 1.
    val motion = vm.moving.firstOrNull()
    val walk = motion as? Walk
    val steps = remember { Animatable(0f) }
    LaunchedEffect(vm.moved, motion != null) {
        val t0 = System.nanoTime()
        steps.snapTo(0f)
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
    PantallaChiva {
        Board(
            vm.config, state, Modifier.fillMaxSize().padding(4.dp), onSquare = { shownSquare = it },
            highlight = if (walk == null) state.players[state.current].position else null, hops = hops,
            marks = cardOf?.let { k -> state.holdings.filterValues { it.owner == k }.keys }.orEmpty(),
            markColor = cardOf?.let { PlayerColors[it] } ?: Color.Unspecified,
            flight = (motion as? Flight)?.let { Fly(it.square, it.player, steps.value.coerceIn(0f, 1f), TINT_MS / (TINT_MS + FLY_MS).toFloat()) },
            iconOf = { icons[it] },
        ) {
            Column(Modifier.fillMaxSize()) {
                PlayersRow(
                    state, showMoney, onTap = { cardOf = if (cardOf == it) null else it }, onLongPress = { showMoney = !showMoney },
                    onPlaced = { k, c -> icons[k] = c },
                )
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
    // Decide el otro teléfono (F5.4) o la máquina (F5.8b): aquí solo se mira el tablero y el letrero.
    if (vm.waitingFor != null || vm.machineTurn != null) return
    when (val phase = state.phase) {
        is TurnPhase.Roll -> if (state.players[state.current].jailTurns != null) JailDialog(vm)
        is TurnPhase.Buy -> BuyDialog(vm, phase.square)
        is TurnPhase.Auction -> AuctionDialog(vm, phase)
        is TurnPhase.TaxChoice -> TaxDialog(vm, phase.square)
        is TurnPhase.Debt -> DebtDialog(vm, phase, askBankruptcy)
        is TurnPhase.Over -> OverDialog(vm, phase.winners, onMenu?.let { { vm.leave(); it() } }) { vm.restart(newSeed()) }
        TurnPhase.EndOfTurn -> Unit
    }
}

@Composable
private fun Center(vm: GameViewModel) {
    val state = vm.state
    val player = state.players[state.current]
    Column(Modifier.fillMaxSize().padding(2.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        Calcomania {
            Text(
                stringResource(R.string.turn_of, player.name), fontSize = 22.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
            vm.lastDice?.let { DiceRow(it) }
            vm.error?.let {
                Text(
                    stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        val waiting = vm.waitingFor
        if (waiting != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconImage(Icon.ENLACE, 28.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (vm.remote?.connected == false) R.string.waiting_cut else R.string.waiting_remote, waiting),
                    fontSize = 20.sp, textAlign = TextAlign.Center,
                )
            }
            return@Column
        }
        val machine = vm.machineTurn
        if (machine != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconImage(Icon.MAQUINA, 28.dp)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.machine_playing, machine), fontSize = 20.sp, textAlign = TextAlign.Center)
            }
            return@Column
        }
        when {
            state.phase == TurnPhase.Roll && player.jailTurns == null ->
                BotonChiva(stringResource(R.string.roll), { vm.act(Action.Roll) })
            state.phase == TurnPhase.EndOfTurn ->
                BotonChiva(stringResource(R.string.end_turn), { vm.act(Action.EndTurn) })
        }
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

@Composable
private fun BuyDialog(vm: GameViewModel, square: Int) {
    val sq = vm.config.squares[square] as OwnableSquare
    DialogoChiva(
        stringResource(R.string.buy_title, vm.state.players[vm.state.current].name, sq.name),
        botones = {
            BotonChiva(stringResource(R.string.buy, money(sq.price)), { vm.act(Action.Buy) })
            BotonChiva(stringResource(R.string.decline), { vm.act(Action.Decline) }, principal = false)
        },
    ) {
        Deed(vm.config, vm.state, square)
        Rejected(vm)
    }
}

@Composable
private fun AuctionDialog(vm: GameViewModel, auction: TurnPhase.Auction) {
    val players = vm.state.players
    val bidder = nextBidder(auction)
    val base = minimumBid(vm.config, auction.square)
    val leader = auction.highestBidder
    val offers = if (leader == null) listOf(base, base + 10, base + 50) else listOf(10, 50, 100).map { auction.highestBid + it }
    DialogoChiva(
        stringResource(R.string.auction_title, vm.config.squares[auction.square].name), color = Chiva.Magenta,
        botones = {
            offers.filter { it <= players[bidder].money }.forEach { amount ->
                BotonChiva(stringResource(R.string.bid, money(amount)), { vm.act(Action.Bid(bidder, amount)) })
            }
            BotonChiva(stringResource(R.string.pass_bid), { vm.act(Action.PassBid(bidder)) }, principal = false)
        },
    ) {
        Text(
            if (leader == null) stringResource(R.string.auction_none, money(base))
            else stringResource(R.string.auction_leader, players[leader].name, money(auction.highestBid)),
            fontSize = 16.sp,
        )
        Text(stringResource(R.string.auction_turn, players[bidder].name, money(players[bidder].money)), fontSize = 18.sp)
        Rejected(vm)
    }
}

@Composable
private fun TaxDialog(vm: GameViewModel, square: Int) {
    val tax = vm.config.squares[square] as Tax
    DialogoChiva(
        stringResource(R.string.tax_title, vm.state.players[vm.state.current].name, tax.name),
        botones = {
            BotonChiva(stringResource(R.string.tax_fixed, money(tax.fixed)), { vm.act(Action.PayTax(percent = false)) })
            if (tax.percent > 0) {
                BotonChiva(stringResource(R.string.tax_percent, tax.percent), { vm.act(Action.PayTax(percent = true)) }, principal = false)
            }
        },
    ) {
        Rejected(vm)
    }
}

@Composable
private fun JailDialog(vm: GameViewModel) {
    val player = vm.state.players[vm.state.current]
    val rules = vm.config.rules
    val turns = player.jailTurns ?: 0
    DialogoChiva(
        stringResource(R.string.jail_title, player.name, turns + 1, rules.jailMaxTurns), color = Chiva.Tinta,
        botones = {
            BotonChiva(stringResource(R.string.jail_roll), { vm.act(Action.Roll) })
            // El motor dice si se puede (R-22): aquí solo se ocultan los botones que seguro no valen.
            if (turns < rules.jailMaxTurns - 1 && player.money >= rules.jailFine) {
                BotonChiva(stringResource(R.string.jail_fine, money(rules.jailFine)), { vm.act(Action.PayJailFine) }, principal = false)
            }
            if (player.jailCards.isNotEmpty()) {
                BotonChiva(stringResource(R.string.jail_card), { vm.act(Action.UseJailCard) }, principal = false)
            }
        },
    ) {
        Rejected(vm)
    }
}

@Composable
private fun DebtDialog(vm: GameViewModel, debt: TurnPhase.Debt, ask: Boolean) {
    val state = vm.state
    val debtor = debt.debts.first().debtor
    val player = state.players[debtor]
    var confirm by remember { mutableStateOf(ask) }
    if (confirm) {
        ConfirmBankruptcy(vm, debtor, debt.debts.first().creditor, onCancel = { confirm = false })
        return
    }
    DialogoChiva(
        stringResource(R.string.debt_title, player.name, money(player.money)),
        botones = {
            state.holdings.filter { it.value.owner == debtor }.toSortedMap().forEach { (square, h) ->
                val name = vm.config.squares[square].name
                if (h.houses > 0 || h.hotel) {
                    BotonChiva(stringResource(R.string.debt_sell, name), { vm.act(Action.SellBuilding(square)) }, principal = false)
                } else if (!h.mortgaged) {
                    BotonChiva(
                        stringResource(R.string.debt_mortgage, name, money(mortgageValue(vm.config, state, square))),
                        { vm.act(Action.Mortgage(square)) }, principal = false,
                    )
                }
            }
            BotonChiva(stringResource(R.string.bankruptcy), { confirm = true })
        },
    ) {
        Text(stringResource(R.string.debt_text), fontSize = 16.sp)
        Rejected(vm)
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
