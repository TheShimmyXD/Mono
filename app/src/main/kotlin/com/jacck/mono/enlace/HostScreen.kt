package com.jacck.mono.enlace

import android.os.Build
import android.provider.Settings
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.jacck.mono.BotonChiva
import com.jacck.mono.Calcomania
import com.jacck.mono.Chiva
import com.jacck.mono.LOG_TAG
import com.jacck.mono.PantallaChiva
import com.jacck.mono.R
import com.jacck.mono.engine.Engine
import com.jacck.mono.engine.link.Host
import com.jacck.mono.engine.link.Message
import com.jacck.mono.engine.model.GameConfig
import kotlinx.coroutines.delay

/** Minutos y segundos, `m:ss`, del tiempo que lleva la conexión. */
fun clock(seconds: Long): String = "%d:%02d".format(seconds / 60, seconds % 60)

/**
 * Sala del anfitrión (F5.3, D-47): crea la partida con los jugadores del menú, la anuncia en la red
 * local ([LanServer], D-48) y atiende al invitado con [hostLoop]; los jugadores de [seats] juegan en el otro teléfono. Muestra
 * con quién está conectado, desde cuándo, los cortes y los mensajes. La pantalla no se apaga: HyperOS
 * corta la red de las apps que pasan a segundo plano.
 */
@Composable
fun HostScreen(config: GameConfig, names: List<String>, tokens: List<String>, seats: Set<Int>, seed: Long, onCancel: () -> Unit) {
    val host = remember { Host(config, Engine.newGame(config, names, seed, tokens).state, seats) }
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
                    LanGate { addresses -> HostLink(host, addresses) }
                }
            }
            Text(stringResource(R.string.host_note), fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
        }
        Column(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp)) {
            BotonChiva(stringResource(R.string.host_cancel), onCancel, principal = false)
        }
    }
}

/** El servidor de la red local y su estado en pantalla; se detiene al salir de la sala. */
@Composable
private fun HostLink(host: Host, addresses: List<String>) {
    val context = LocalContext.current
    val label = remember { Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME) ?: Build.MODEL }
    var event by remember { mutableStateOf<LinkEvent?>(null) }
    var port by remember { mutableStateOf<Int?>(null) }
    var guest by remember { mutableStateOf("?") }
    var since by remember { mutableStateOf(0L) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var cuts by remember { mutableStateOf(0) }
    var messages by remember { mutableStateOf(0) }
    DisposableEffect(Unit) {
        val main = Handler(Looper.getMainLooper())
        val server = LanServer(context, label, { e ->
            Log.i(LOG_TAG, "anfitrión: $e")
            main.post {
                if (e is LinkEvent.Listening) port = e.port
                if (e is LinkEvent.Connected) { since = System.currentTimeMillis(); messages = 0; guest = "?" }
                if (e is LinkEvent.Closed && event is LinkEvent.Connected) {
                    cuts++
                    Log.i(LOG_TAG, "anfitrión: corte $cuts tras ${clock((System.currentTimeMillis() - since) / 1000)}")
                }
                event = e
            }
        }) { input, output ->
            val (read, bad) = hostLoop(host, input, output) { m ->
                if (m is Message.Hello) Log.i(LOG_TAG, "anfitrión: hola de ${m.name}, protocolo ${m.version}")
                main.post {
                    messages++
                    if (m is Message.Hello) guest = m.name
                }
            }
            Log.i(LOG_TAG, "anfitrión: fin de la sesión, $read mensajes ($bad sin entender)")
        }
        server.start()
        onDispose { server.stop() }
    }
    LaunchedEffect(event) {
        while (event is LinkEvent.Connected) { now = System.currentTimeMillis(); delay(1000) }
    }
    Text(stringResource(R.string.host_phone, label), fontSize = 16.sp)
    port?.let { p -> Text(stringResource(R.string.host_address, addresses.joinToString(" · ") { "$it:$p" }), fontSize = 16.sp) }
    Text(
        when (val e = event) {
            null, is LinkEvent.Listening -> stringResource(R.string.host_listening)
            is LinkEvent.Connected -> stringResource(R.string.host_connected, e.peer, guest)
            is LinkEvent.Closed -> stringResource(R.string.host_closed, e.reason ?: "—")
        },
        fontSize = 18.sp,
    )
    if (event is LinkEvent.Connected) {
        Text(stringResource(R.string.host_time, clock((now - since).coerceAtLeast(0) / 1000), cuts, messages), fontSize = 16.sp)
    } else if (cuts > 0) {
        Text(stringResource(R.string.host_cuts, cuts), fontSize = 16.sp)
    }
}
