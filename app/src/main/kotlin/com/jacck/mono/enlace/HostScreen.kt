package com.jacck.mono.enlace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.model.GameConfig
import com.jacck.mono.game.GameScreen
import com.jacck.mono.game.GameViewModel
import kotlinx.coroutines.delay

/** Minutos y segundos, `m:ss`, del tiempo que lleva la conexión. */
fun clock(seconds: Long): String = "%d:%02d".format(seconds / 60, seconds % 60)

/**
 * Partida con otro teléfono (F5.4, D-51), del lado del anfitrión: crea la partida con los jugadores del
 * menú y su [HostRoom] dentro de un `GameViewModel` con la clave [key] (una nueva por cada sala); muestra
 * la sala hasta que llega el saludo del invitado y entonces la partida. [onCancel] = salir de la sala.
 */
@Composable
fun LinkedGame(key: String, config: GameConfig, names: List<String>, tokens: List<String>, seats: Set<Int>, seed: Long, onCancel: () -> Unit) {
    val context = LocalContext.current
    val vm = viewModel(key = key) {
        val host = Host(config, Engine.newGame(config, names, seed, tokens).state, seats)
        GameViewModel(config, names, seed, resumed = host.state, tokens = tokens, remote = HostRoom(context, host))
    }
    val room = vm.remote as HostRoom
    if (room.started) {
        GameScreen(vm) { System.currentTimeMillis() }
    } else {
        HostScreen(room, config, names, seats) {
            room.close()
            onCancel()
        }
    }
}

/**
 * Sala del anfitrión (F5.3, D-47): la partida que se va a jugar, la sala en la red local ([LanServer],
 * D-48; se abre al tener red) y con quién está conectado, desde cuándo, los cortes y los mensajes; los
 * jugadores de [seats] juegan en el otro teléfono. La pantalla no se apaga: HyperOS corta la red de
 * las apps que pasan a segundo plano.
 */
@Composable
fun HostScreen(room: HostRoom, config: GameConfig, names: List<String>, seats: Set<Int>, onCancel: () -> Unit) {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    BackHandler(onBack = onCancel)
    PantallaChiva {
        Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.host_title), fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.host_board, config.name, config.squares.size), fontSize = 20.sp)
                    Text(stringResource(R.string.host_here, names.filterIndexed { i, _ -> i !in seats }.joinToString(", ")), fontSize = 16.sp)
                    Text(stringResource(R.string.host_remote, names.filterIndexed { i, _ -> i in seats }.joinToString(", ")), fontSize = 16.sp)
                }
            }
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanGate { addresses ->
                        LaunchedEffect(Unit) { room.start() }
                        RoomStatus(room, addresses)
                    }
                }
            }
            Text(stringResource(R.string.host_note), fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
        }
        Column(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp)) {
            BotonChiva(stringResource(R.string.host_cancel), onCancel, principal = false)
        }
    }
}

/** El estado de la sala en pantalla: dirección, conexión, tiempo, cortes y mensajes. */
@Composable
private fun RoomStatus(room: HostRoom, addresses: List<String>) {
    val event = room.event
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(event) {
        while (event is LinkEvent.Connected) { now = System.currentTimeMillis(); delay(1000) }
    }
    Text(stringResource(R.string.host_phone, room.label), fontSize = 16.sp)
    room.port?.let { p -> Text(stringResource(R.string.host_address, addresses.joinToString(" · ") { "$it:$p" }), fontSize = 16.sp) }
    Text(
        when (event) {
            null, is LinkEvent.Listening -> stringResource(R.string.host_listening)
            is LinkEvent.Connected -> stringResource(R.string.host_connected, event.peer, room.guest)
            is LinkEvent.Closed -> stringResource(R.string.host_closed, event.reason ?: "—")
        },
        fontSize = 18.sp,
    )
    if (event is LinkEvent.Connected) {
        Text(stringResource(R.string.host_time, clock((now - room.since).coerceAtLeast(0) / 1000), room.cuts, room.messages), fontSize = 16.sp)
    } else if (room.cuts > 0) {
        Text(stringResource(R.string.host_cuts, room.cuts), fontSize = 16.sp)
    }
}
