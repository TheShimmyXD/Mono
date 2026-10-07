package com.jacck.mono.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jacck.mono.R
import com.jacck.mono.engine.Dice
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.engine.model.GameState
import com.jacck.mono.engine.model.Holding
import com.jacck.mono.engine.model.OwnableSquare
import com.jacck.mono.engine.model.Property
import com.jacck.mono.engine.model.Station
import com.jacck.mono.engine.model.Tax
import com.jacck.mono.engine.model.Utility
import com.jacck.mono.engine.mortgageValue
import com.jacck.mono.engine.rentDue
import java.text.Normalizer

private val Ink = Color(0xFF1B1B1B)
private val Sky = Color(0xFF38C6F4)
private val Now = Color(0xFFFFE08A)
private val Stripe = listOf(0xFFE63946, 0xFFFFC21A, 0xFF1D7BEF, 0xFF2BB04A, 0xFFE5007E).map { Color(it) }

/** Clave del arte de un lugar: el nombre sin tildes, en minúsculas y con `_` (como en `arte/arte.py`). */
fun artKey(name: String): String =
    Normalizer.normalize(name, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()
        .replace(Regex("[^a-z0-9]+"), "_").trim('_')

/**
 * Carta de chiva de una casilla (FA.3, D-30): se abre al tocarla en el tablero y se cierra tocando
 * fuera. Arte del lugar (si ya lo tiene) o su ícono, nombre en la franja del grupo, alquileres con
 * la fila que corresponde ahora resaltada, precios, hipoteca y dueño. Cifras del motor.
 */
@Composable
fun SquareCard(config: GameConfig, state: GameState, square: Int, onClose: () -> Unit) {
    val sq = config.squares[square]
    val holding = state.holdings[square]
    val group = (sq as? Property)?.let { p -> config.groups.firstOrNull { it.id == p.group } }
    val band = group?.let { Color(android.graphics.Color.parseColor(it.color)) } ?: Ink
    Dialog(onDismissRequest = onClose) {
        Card(Modifier.fillMaxWidth().border(3.dp, Ink, RoundedCornerShape(16.dp)), shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ChivaStripe()
                Box(Modifier.fillMaxWidth().background(band).padding(8.dp), contentAlignment = Alignment.Center) {
                    Text(sq.name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, textAlign = TextAlign.Center)
                }
                val art = ArteLugares[artKey(sq.name)]
                val icon = sq.icon()
                if (art != null) {
                    Image(painterResource(art), null, Modifier.fillMaxWidth().aspectRatio(200f / 140f), contentScale = ContentScale.FillWidth)
                } else if (icon != null) {
                    Box(Modifier.fillMaxWidth().height(120.dp).background(Sky), contentAlignment = Alignment.Center) { IconImage(icon, 88.dp) }
                }
                if (art != null || icon != null) ChivaStripe()
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    when (sq) {
                        is OwnableSquare -> OwnableDetails(config, state, square, sq, holding, group?.name)
                        is Tax -> TaxDetails(sq)
                        else -> Unit
                    }
                }
                ChivaStripe()
            }
        }
    }
}

@Composable
private fun OwnableDetails(config: GameConfig, state: GameState, square: Int, sq: OwnableSquare, holding: Holding?, group: String?) {
    val price = stringResource(R.string.money, sq.price)
    Text(if (group != null) stringResource(R.string.card_group_price, group, price) else stringResource(R.string.deed_price, price), fontWeight = FontWeight.Bold)
    val active = holding != null && !holding.mortgaged
    when (sq) {
        is Property -> {
            val row = when { !active -> -1; holding!!.hotel -> sq.rents.lastIndex; else -> holding.houses }
            sq.rents.forEachIndexed { k, r ->
                val hotel = k == sq.rents.lastIndex && k > 0
                val label = when {
                    k == 0 -> stringResource(R.string.no_houses)
                    hotel -> stringResource(R.string.card_hotel)
                    else -> pluralStringResource(R.plurals.card_houses, k, k)
                }
                RentLine(label, stringResource(R.string.money, r), k == row) {
                    when {
                        hotel -> IconImage(Icon.HOTEL, 14.dp)
                        k > 0 -> BuildingIcons(Holding(0, houses = k), 14.dp)
                    }
                }
            }
            val house = sq.housePrice ?: config.rules.housePrice
            val hotel = sq.hotelPrice ?: config.rules.hotelPrice
            if (house != null && hotel != null) {
                Text(stringResource(R.string.card_prices, stringResource(R.string.money, house), stringResource(R.string.money, hotel)), fontSize = 13.sp)
            }
        }
        is Station -> {
            val owned = holding?.let { h -> state.holdings.count { it.value.owner == h.owner && config.squares[it.key] is Station } } ?: 0
            sq.rents.forEachIndexed { k, r ->
                RentLine(pluralStringResource(R.plurals.card_stations, k + 1, k + 1), stringResource(R.string.money, r), active && k + 1 == owned) {}
            }
        }
        is Utility -> sq.diceMultipliers.forEachIndexed { k, m ->
            Text(stringResource(R.string.deed_utility, m, k + 1), fontSize = 13.sp)
        }
    }
    runCatching { mortgageValue(config, state, square) }.getOrNull()?.let {
        Text(stringResource(R.string.deed_mortgage, stringResource(R.string.money, it)), fontSize = 13.sp)
    }
    Spacer(Modifier.height(4.dp))
    if (holding == null) {
        Text(stringResource(R.string.card_free), fontWeight = FontWeight.Bold)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Token(PlayerColors[holding.owner], 14.dp)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.card_owner, state.players[holding.owner].name), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            if (holding.mortgaged) Text(stringResource(R.string.mortgaged_short)) else BuildingIcons(holding, 18.dp)
        }
        if (active && sq !is Utility) {
            Text(stringResource(R.string.card_rent_now, stringResource(R.string.money, rentDue(config, state, square, Dice(1, 1)))), fontSize = 13.sp)
        }
    }
}

@Composable
private fun TaxDetails(sq: Tax) {
    Text(stringResource(R.string.card_tax, stringResource(R.string.money, sq.fixed)), fontWeight = FontWeight.Bold)
    if (sq.percent > 0) Text(stringResource(R.string.card_tax_percent, sq.percent), fontSize = 13.sp)
    if (sq.perHotel > 0) Text(stringResource(R.string.card_tax_hotel, stringResource(R.string.money, sq.perHotel)), fontSize = 13.sp)
}

/** Fila de alquiler; [now] la resalta (la que se cobra con lo que hay construido). */
@Composable
private fun RentLine(label: String, amount: String, now: Boolean, icon: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(if (now) Now else Color.Transparent).padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Text(label, Modifier.weight(1f).padding(start = 4.dp), fontSize = 14.sp)
        Text(amount, fontWeight = if (now) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
    }
}

/** Franja de chiva: dientes de colores sobre negro (D-28). */
@Composable
private fun ChivaStripe() {
    Row(Modifier.fillMaxWidth().height(8.dp).background(Ink)) {
        repeat(20) { Box(Modifier.weight(1f).fillMaxSize().padding(horizontal = 1.dp, vertical = 2.dp).background(Stripe[it % Stripe.size])) }
    }
}
