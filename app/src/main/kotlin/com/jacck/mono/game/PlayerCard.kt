package com.jacck.mono.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.R
import com.jacck.mono.board.BuildingIcons
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.engine.model.Action
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.Property

/**
 * Un jugador en la ventana (FD.6, D-72; antes tarjeta en el centro y hoja que subía, FC.3 y F3.4):
 * medallón, nombre, cuántas propiedades y dinero; debajo sus casillas en orden del anillo, con su
 * franja y sus edificios. Si [manage], un botón por cada jugada que el motor acepta ahora
 * (`propertyMoves`), el último rechazo y «Declararme en quiebra» (D-56); si no, solo se mira.
 */
@Composable
fun PlayerDetail(vm: GameViewModel, player: Int, manage: Boolean, onBankrupt: () -> Unit) {
    val state = vm.state
    val p = state.players[player]
    val mine = state.holdings.filter { it.value.owner == player }.toSortedMap()
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlayerToken(state, player, 44.dp)
            Column(Modifier.weight(1f)) {
                Text(p.name, fontSize = 18.sp, maxLines = 1)
                Text(stringResource(if (p.bankrupt) R.string.card_bankrupt else R.string.card_properties, mine.size), fontSize = 13.sp)
            }
            Text(money(p.money), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        if (manage) vm.error?.let { Text(stringResource(R.string.rejected, it), color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        if (mine.isEmpty()) Text(stringResource(R.string.properties_none), fontSize = 15.sp)
        mine.forEach { (square, holding) -> PropertyRow(vm, square, holding, manage) }
        if (manage) BotonChiva(stringResource(R.string.bankruptcy_me), onBankrupt, principal = false)
    }
}

/** Una propiedad del jugador: franja del grupo, nombre, estado y edificios; debajo, sus jugadas. */
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
    Calcomania(sombra = 2.dp, borde = 1.5.dp, forma = RoundedCornerShape(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp, 40.dp).background(band))
            Column(Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(sq.name, fontSize = 14.sp, maxLines = 1)
                status?.let { Text(it, fontSize = 12.sp) }
            }
            if (!holding.mortgaged) BuildingIcons(holding, 14.dp)
            Box(Modifier.size(6.dp))
        }
        if (moves.isNotEmpty()) {
            Row(Modifier.padding(start = 6.dp, end = 9.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
