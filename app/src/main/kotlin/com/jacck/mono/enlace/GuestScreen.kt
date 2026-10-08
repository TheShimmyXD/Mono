package com.jacck.mono.enlace

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jacck.mono.game.GameScreen
import com.jacck.mono.game.GameViewModel

/** «10.0.1.192:46199» → dirección y puerto; null si no tiene esa forma. */
fun parseAddress(text: String): Pair<String, Int>? {
    val m = Regex("""^\s*(\d{1,3}(?:\.\d{1,3}){3})\s*:\s*(\d{1,5})\s*$""").find(text) ?: return null
    val port = m.groupValues[2].toInt()
    return if (port in 1..65535 && m.groupValues[1].split('.').all { it.toInt() <= 255 }) m.groupValues[1] to port else null
}

/**
 * Unirse a la partida de un amigo (F5.3, F5.10, D-47, D-48, D-76): busca salas `_mono._tcp` en la red
 * local (NSD) y, al tocar una (o al escribir la dirección que muestra la sala), se conecta con un
 * [GuestRoom] (un ViewModel con la clave [key], uno nuevo cada vez). Cuando llega la partida abre el
 * tablero y se juega como en la sala; con [machine] los jugadores de aquí los juega la máquina, y con
 * [address] («ip:puerto») se conecta sin tocar nada (para probar el enlace sin nadie al teléfono).
 * La pantalla no se apaga: HyperOS corta la red en segundo plano.
 */
@Composable
fun GuestScreen(key: String, machine: Boolean = false, address: String? = null, onCancel: () -> Unit) {
    val context = LocalContext.current
    val room = viewModel(key = key) { GuestRoom(context.applicationContext) }
    LaunchedEffect(room) { address?.let(::parseAddress)?.let { (ip, port) -> room.connect(ip, ip, port) } }
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    val leave = { room.close(); onCancel() }
    val start = room.start
    if (start != null) {
        val vm = viewModel(key = "$key-partida") {
            GameViewModel(start.config, start.state.players.map { it.name }, 0L, resumed = start.state, remote = room, bots = if (machine) start.mine else emptySet())
        }
        GameScreen(vm, onMenu = leave) { System.currentTimeMillis() }
        return
    }
    BackHandler(onBack = leave)
    PantallaChiva {
        Column(
            Modifier.weight(1f).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.join_title), fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanGate { JoinLink(room) }
                }
            }
        }
        Column(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp)) {
            BotonChiva(stringResource(R.string.host_cancel), leave, principal = false)
        }
    }
}

@Composable
private fun JoinLink(room: GuestRoom) {
    val context = LocalContext.current
    val main = remember { Handler(Looper.getMainLooper()) }
    val nsd = remember { context.getSystemService(NsdManager::class.java) }
    val found = remember { mutableStateListOf<NsdServiceInfo>() }
    var typed by rememberSaveable { mutableStateOf("") }

    // Búsqueda de salas mientras la pantalla está abierta; hasta Android 13 sin «T extensions 7»
    // el mDNS no llega sin MulticastLock (NsdManager.java del SDK 37).
    DisposableEffect(Unit) {
        val lock = context.applicationContext.getSystemService(WifiManager::class.java)?.createMulticastLock("mono")?.apply { acquire() }
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(type: String) { Log.i(LOG_TAG, "invitado: buscando salas $type") }
            override fun onDiscoveryStopped(type: String) {}
            override fun onStartDiscoveryFailed(type: String, code: Int) { Log.i(LOG_TAG, "invitado: no se pudo buscar (código $code)") }
            override fun onStopDiscoveryFailed(type: String, code: Int) {}
            override fun onServiceFound(info: NsdServiceInfo) {
                Log.i(LOG_TAG, "invitado: sala «${info.serviceName}»")
                main.post { if (found.none { it.serviceName == info.serviceName }) found.add(info) }
            }
            override fun onServiceLost(info: NsdServiceInfo) {
                Log.i(LOG_TAG, "invitado: se fue «${info.serviceName}»")
                main.post { found.removeAll { it.serviceName == info.serviceName } }
            }
        }
        nsd?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        onDispose {
            try { nsd?.stopServiceDiscovery(listener) } catch (_: IllegalArgumentException) {}
            lock?.release()
        }
    }

    // Resuelve la sala elegida (su dirección y puerto) y se conecta.
    @Suppress("DEPRECATION") // resolveService va desde la API 16; registerServiceInfoCallback solo desde la 34
    val pick: (NsdServiceInfo) -> Unit = { info ->
        nsd?.resolveService(info, object : NsdManager.ResolveListener {
            override fun onServiceResolved(r: NsdServiceInfo) {
                val ip = r.host?.hostAddress
                main.post { if (ip == null) room.failed(r.serviceName, "sin dirección") else room.connect(r.serviceName, ip, r.port) }
            }
            override fun onResolveFailed(i: NsdServiceInfo, code: Int) {
                Log.i(LOG_TAG, "invitado: no se pudo resolver «${i.serviceName}» (código $code)")
                main.post { room.failed(i.serviceName, "código $code") }
            }
        })
    }

    when (val j = room.status) {
        Join.Choosing -> {
            Text(stringResource(if (found.isEmpty()) R.string.join_searching else R.string.join_choose), fontSize = 18.sp)
            found.forEach { info -> BotonChiva(info.serviceName, { pick(info) }, principal = false) }
            Text(stringResource(R.string.join_by_address), fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(typed, { typed = it.take(21) }, placeholder = { Text(stringResource(R.string.join_example), color = Chiva.Tinta.copy(alpha = 0.4f)) }, singleLine = true, modifier = Modifier.weight(1f))
                val address = parseAddress(typed)
                BotonChiva(stringResource(R.string.join_connect), { address?.let { (ip, port) -> room.connect(ip, ip, port) } }, Modifier.weight(0.6f), enabled = address != null)
            }
        }
        is Join.Connecting -> Text(stringResource(R.string.join_connecting, j.peer), fontSize = 18.sp)
        is Join.Connected -> Text(stringResource(R.string.join_connecting, j.peer), fontSize = 18.sp)
        is Join.Lost -> Text(stringResource(R.string.join_connecting, j.peer), fontSize = 18.sp)
        is Join.Closed -> {
            Text(stringResource(R.string.join_closed, j.peer, j.reason ?: "—"), fontSize = 18.sp)
            BotonChiva(stringResource(R.string.join_again), room::choose)
        }
    }
}
