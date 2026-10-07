package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Box(Modifier.fillMaxSize().background(Color(0xFF2E5E4E)).safeDrawingPadding()) {
        Board(vm.config, state, Modifier.fillMaxSize().padding(2.dp), onSquare = { shownSquare = it }) {
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
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
        Text(
            stringResource(R.string.turn_of, player.name), fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        vm.lastDice?.let { DiceRow(it) }
        PlayersPanel(state)
        vm.error?.let {
            Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        if (state.phase == TurnPhase.Roll || state.phase == TurnPhase.EndOfTurn) {
            OutlinedButton(onClick = onProperties, modifier = Modifier.fillMaxWidth()) {
                IconImage(Icon.CASA, 20.dp)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.my_properties))
            }
        }
        when {
            state.phase == TurnPhase.Roll && player.jailTurns == null ->
                Button(onClick = { vm.act(Action.Roll) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.roll)) }
            state.phase == TurnPhase.EndOfTurn ->
                Button(onClick = { vm.act(Action.EndTurn) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.end_turn)) }
        }
    }
}

@Composable
private fun DiceRow(dice: Dice) {
    Row(
        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconImage(Icon.dado(dice.first), 52.dp)
        IconImage(Icon.dado(dice.second), 52.dp)
        Text("= ${dice.total}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
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

/** Diálogo que solo tiene botones dentro del cuerpo: subasta, Cárcel, deuda. */
@Composable
private fun ChoiceDialog(title: String, body: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) { body() } },
        confirmButton = {},
    )
}

@Composable
private fun NoticesDialog(lines: List<String>, onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.what_happened)) },
        text = {
            Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                lines.forEach { Text("• $it") }
            }
        },
        confirmButton = { Button(onClick = onDone) { Text(stringResource(R.string.next)) } },
    )
}

@Composable
private fun BuyDialog(vm: GameViewModel, square: Int) {
    val sq = vm.config.squares[square] as OwnableSquare
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.buy_title, vm.state.players[vm.state.current].name, sq.name)) },
        text = { Deed(vm.config, vm.state, square) },
        confirmButton = { Button(onClick = { vm.act(Action.Buy) }) { Text(stringResource(R.string.buy, money(sq.price))) } },
        dismissButton = { TextButton(onClick = { vm.act(Action.Decline) }) { Text(stringResource(R.string.decline)) } },
    )
}

@Composable
private fun AuctionDialog(vm: GameViewModel, auction: TurnPhase.Auction) {
    val players = vm.state.players
    val bidder = nextBidder(auction)
    val base = minimumBid(vm.config, auction.square)
    ChoiceDialog(stringResource(R.string.auction_title, vm.config.squares[auction.square].name)) {
        val leader = auction.highestBidder
        Text(
            if (leader == null) stringResource(R.string.auction_none, money(base))
            else stringResource(R.string.auction_leader, players[leader].name, money(auction.highestBid)),
        )
        Text(stringResource(R.string.auction_turn, players[bidder].name, money(players[bidder].money)), fontWeight = FontWeight.Bold)
        val offers = if (leader == null) listOf(base, base + 10, base + 50) else listOf(10, 50, 100).map { auction.highestBid + it }
        offers.filter { it <= players[bidder].money }.forEach { amount ->
            Button(onClick = { vm.act(Action.Bid(bidder, amount)) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.bid, money(amount)))
            }
        }
        OutlinedButton(onClick = { vm.act(Action.PassBid(bidder)) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.pass_bid))
        }
        vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
    }
}

@Composable
private fun TaxDialog(vm: GameViewModel, square: Int) {
    val tax = vm.config.squares[square] as Tax
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.tax_title, vm.state.players[vm.state.current].name, tax.name)) },
        text = null,
        confirmButton = { Button(onClick = { vm.act(Action.PayTax(percent = false)) }) { Text(stringResource(R.string.tax_fixed, money(tax.fixed))) } },
        dismissButton = {
            if (tax.percent > 0) {
                TextButton(onClick = { vm.act(Action.PayTax(percent = true)) }) { Text(stringResource(R.string.tax_percent, tax.percent)) }
            }
        },
    )
}

@Composable
private fun JailDialog(vm: GameViewModel) {
    val player = vm.state.players[vm.state.current]
    val rules = vm.config.rules
    val turns = player.jailTurns ?: 0
    ChoiceDialog(stringResource(R.string.jail_title, player.name, turns + 1, rules.jailMaxTurns)) {
        Button(onClick = { vm.act(Action.Roll) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.jail_roll)) }
        // El motor dice si se puede (R-22): aquí solo se ocultan los botones que seguro no valen.
        if (turns < rules.jailMaxTurns - 1 && player.money >= rules.jailFine) {
            OutlinedButton(onClick = { vm.act(Action.PayJailFine) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.jail_fine, money(rules.jailFine)))
            }
        }
        if (player.jailCards.isNotEmpty()) {
            OutlinedButton(onClick = { vm.act(Action.UseJailCard) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.jail_card))
            }
        }
        vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
    }
}

@Composable
private fun DebtDialog(vm: GameViewModel, debt: TurnPhase.Debt) {
    val state = vm.state
    val debtor = debt.debts.first().debtor
    val player = state.players[debtor]
    ChoiceDialog(stringResource(R.string.debt_title, player.name, money(player.money))) {
        Text(stringResource(R.string.debt_text))
        state.holdings.filter { it.value.owner == debtor }.toSortedMap().forEach { (square, h) ->
            val name = vm.config.squares[square].name
            if (h.houses > 0 || h.hotel) {
                OutlinedButton(onClick = { vm.act(Action.SellBuilding(square)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.debt_sell, name))
                }
            } else if (!h.mortgaged) {
                OutlinedButton(onClick = { vm.act(Action.Mortgage(square)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.debt_mortgage, name, money(mortgageValue(vm.config, state, square))))
                }
            }
        }
        Button(onClick = { vm.act(Action.DeclareBankruptcy) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bankruptcy))
        }
        vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
    }
}

@Composable
private fun OverDialog(vm: GameViewModel, winners: List<Int>, onRestart: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.game_over)) },
        text = { Text(stringResource(R.string.winners, winners.joinToString(" y ") { vm.state.players[it].name })) },
        confirmButton = { Button(onClick = onRestart) { Text(stringResource(R.string.new_game)) } },
    )
}

/** Escritura de una casilla con dueño posible: franja del grupo, precio, alquileres e hipoteca. */
@Composable
private fun Deed(config: GameConfig, state: GameState, square: Int) {
    val sq = config.squares[square] as OwnableSquare
    val band = (sq as? Property)?.let { p -> config.groups.firstOrNull { it.id == p.group } }
        ?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Color.LightGray
    Card(Modifier.fillMaxWidth()) {
        Column {
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
}
