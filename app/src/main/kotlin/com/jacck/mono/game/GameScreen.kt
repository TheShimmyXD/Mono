package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.jacck.mono.board.BuildingIcons
import com.jacck.mono.board.Icon
import com.jacck.mono.board.IconImage
import com.jacck.mono.board.PlayerColors
import com.jacck.mono.board.PlayersPanel
import com.jacck.mono.board.SquareCard
import com.jacck.mono.board.Token
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
 * jugadores y la acción principal; cada decisión del motor sale en un diálogo encima. Las
 * propiedades de quien juega se manejan en una hoja que sube desde abajo (F3.4, D-24).
 */
@Composable
fun GameScreen(vm: GameViewModel, openProperties: Boolean = false, openSquare: Int? = null, newSeed: () -> Long) {
    val state = vm.state
    var showProperties by remember { mutableStateOf(openProperties) }
    var shownSquare by remember { mutableStateOf(openSquare) }
    PantallaChiva {
        Board(vm.config, state, Modifier.fillMaxSize().padding(4.dp), onSquare = { shownSquare = it }) {
            Center(vm) { showProperties = true }
        }
    }
    // La carta de una casilla (FA.3) va primero: los diálogos del turno vuelven al cerrarla.
    shownSquare?.let {
        SquareCard(vm.config, state, it) { shownSquare = null }
        return
    }
    if (showProperties && (state.phase == TurnPhase.Roll || state.phase == TurnPhase.EndOfTurn)) {
        PropertiesSheet(vm) { showProperties = false }
        return
    }
    val lines = vm.notices.mapNotNull { eventLine(it, vm.config, state) }
    if (lines.isNotEmpty()) {
        NoticesDialog(lines, vm::dismissNotices)
        return
    }
    when (val phase = state.phase) {
        is TurnPhase.Roll -> if (state.players[state.current].jailTurns != null) JailDialog(vm)
        is TurnPhase.Buy -> BuyDialog(vm, phase.square)
        is TurnPhase.Auction -> AuctionDialog(vm, phase)
        is TurnPhase.TaxChoice -> TaxDialog(vm, phase.square)
        is TurnPhase.Debt -> DebtDialog(vm, phase)
        is TurnPhase.Over -> OverDialog(vm, phase.winners) { vm.restart(newSeed()) }
        TurnPhase.EndOfTurn -> Unit
    }
}

@Composable
private fun Center(vm: GameViewModel, onProperties: () -> Unit) {
    val state = vm.state
    val player = state.players[state.current]
    Column(Modifier.fillMaxSize().padding(2.dp), verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        Calcomania {
            Text(
                stringResource(R.string.turn_of, player.name), fontSize = 22.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            )
            vm.lastDice?.let { DiceRow(it) }
            PlayersPanel(state, Modifier.padding(horizontal = 10.dp).padding(top = 4.dp, bottom = 8.dp))
            vm.error?.let {
                Text(
                    stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        if (state.phase == TurnPhase.Roll || state.phase == TurnPhase.EndOfTurn) {
            BotonChiva(stringResource(R.string.my_properties), onProperties, principal = false, icono = Icon.CASA)
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
 * Hoja «Mis propiedades» (F3.4, D-24): las casillas de quien juega en orden del anillo, con su
 * franja, sus edificios y un botón por cada jugada que el motor acepta ahora (`propertyMoves`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertiesSheet(vm: GameViewModel, onClose: () -> Unit) {
    val state = vm.state
    val owner = state.current
    val mine = state.holdings.filter { it.value.owner == owner }.toSortedMap()
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Token(PlayerColors[owner], 14.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.properties_title, state.players[owner].name), fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                )
                Text(money(state.players[owner].money), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            if (mine.isEmpty()) Text(stringResource(R.string.properties_none))
            mine.forEach { (square, holding) -> PropertyRow(vm, square, holding) }
        }
    }
}

@Composable
private fun PropertyRow(vm: GameViewModel, square: Int, holding: Holding) {
    val sq = vm.config.squares[square]
    val band = (sq as? Property)?.let { p -> vm.config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Color.LightGray
    val status = when {
        holding.mortgaged -> stringResource(R.string.mortgaged_short)
        holding.hotel || holding.houses > 0 -> null
        sq is Property -> stringResource(R.string.no_houses)
        else -> null
    }
    val small = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(10.dp, 36.dp).background(band))
        Column(Modifier.weight(1f)) {
            Text(sq.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            status?.let { Text(it, fontSize = 12.sp) }
            if (!holding.mortgaged) BuildingIcons(holding, 16.dp)
        }
        propertyMoves(vm.config, vm.state, square).forEach { move ->
            val amount = money(move.amount)
            when (move.action) {
                is Action.Build -> FilledTonalButton(onClick = { vm.act(move.action) }, contentPadding = small) {
                    Text(stringResource(if (move.hotel) R.string.move_hotel else R.string.move_house, amount))
                }
                is Action.SellBuilding -> OutlinedButton(onClick = { vm.act(move.action) }, contentPadding = small) {
                    Text(stringResource(R.string.move_sell, amount))
                }
                is Action.Mortgage -> OutlinedButton(onClick = { vm.act(move.action) }, contentPadding = small) {
                    Text(stringResource(R.string.move_mortgage, amount))
                }
                is Action.Unmortgage -> Button(onClick = { vm.act(move.action) }, contentPadding = small) {
                    Text(stringResource(R.string.move_unmortgage, amount))
                }
                else -> Unit
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
private fun DebtDialog(vm: GameViewModel, debt: TurnPhase.Debt) {
    val state = vm.state
    val debtor = debt.debts.first().debtor
    val player = state.players[debtor]
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
            BotonChiva(stringResource(R.string.bankruptcy), { vm.act(Action.DeclareBankruptcy) })
        },
    ) {
        Text(stringResource(R.string.debt_text), fontSize = 16.sp)
        Rejected(vm)
    }
}

@Composable
private fun OverDialog(vm: GameViewModel, winners: List<Int>, onRestart: () -> Unit) {
    DialogoChiva(
        stringResource(R.string.game_over), color = Chiva.Verde,
        botones = { BotonChiva(stringResource(R.string.new_game), onRestart) },
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
