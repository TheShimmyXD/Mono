package com.jacck.mono.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.R
import com.jacck.mono.board.Icon
import com.jacck.mono.board.PlayerToken
import com.jacck.mono.engine.model.GameState

/**
 * La tarjeta de un jugador en el centro del tablero, en lugar del turno (FC.3, D-60, maqueta E):
 * medallón, nombre, dinero, «Propiedades (n)» y «Cerrar». Mientras está abierta, el tablero marca
 * sus casillas con su color.
 */
@Composable
fun PlayerCard(state: GameState, player: Int, onProperties: () -> Unit, onClose: () -> Unit) {
    val p = state.players[player]
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Calcomania {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PlayerToken(state, player, 64.dp)
                Column {
                    Text(p.name, fontSize = 22.sp)
                    Text(stringResource(R.string.money, p.money), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    if (p.bankrupt) Text(stringResource(R.string.card_bankrupt), fontSize = 14.sp)
                }
            }
        }
        val count = state.holdings.count { it.value.owner == player }
        BotonChiva(stringResource(R.string.card_properties, count), onProperties, icono = Icon.CASA)
        BotonChiva(stringResource(R.string.close), onClose, principal = false)
    }
}
