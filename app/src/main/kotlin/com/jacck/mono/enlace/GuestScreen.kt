package com.jacck.mono.enlace

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.jacck.mono.engine.link.Guest
import com.jacck.mono.engine.link.Message
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Unirse a la partida de un amigo (F5.3, D-47): lista los teléfonos emparejados (solo pide
 * `BLUETOOTH_CONNECT`, sin buscar cercanos) y, al tocar uno, se conecta a su sala por RFCOMM y
 * atiende el enlace con [guestLoop]. Muestra el tablero y quién juega en este teléfono.
 */
@Composable
fun GuestScreen(onCancel: () -> Unit) {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true // HyperOS corta el Bluetooth en segundo plano
        onDispose { view.keepScreenOn = false }
    }
    BackHandler(onBack = onCancel)
    PantallaChiva {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.join_title), fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Calcomania {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BluetoothGate { adapter -> JoinLink(adapter) }
                }
            }
        }
        Column(Modifier.padding(start = 16.dp, end = 19.dp, bottom = 14.dp, top = 4.dp)) {
            BotonChiva(stringResource(R.string.host_cancel), onCancel, principal = false)
        }
    }
}

/** En qué va la conexión del invitado. */
private sealed interface Join {
    data object Choosing : Join
    data class Connecting(val peer: String) : Join
    data class Connected(val peer: String, val board: String?, val mine: List<String>) : Join
    data class Closed(val peer: String, val reason: String?) : Join
}

@SuppressLint("MissingPermission") // dentro de BluetoothGate, con BLUETOOTH_CONNECT concedido
@Composable
private fun JoinLink(adapter: BluetoothAdapter) {
    var join by remember { mutableStateOf<Join>(Join.Choosing) }
    var socket by remember { mutableStateOf<BluetoothSocket?>(null) }
    val paired = remember { adapter.bondedDevices.orEmpty().sortedBy { it.name ?: it.address } }
    DisposableEffect(Unit) { onDispose { socket?.close() } }
    val main = remember { Handler(Looper.getMainLooper()) }
    val connect: (BluetoothDevice) -> Unit = { device ->
        val peer = device.name ?: device.address
        join = Join.Connecting(peer)
        Log.i(LOG_TAG, "invitado: conectando con $peer")
        thread(name = "mono-invitado") {
            val guest = Guest(adapter.name ?: "Mono")
            try {
                // `connect` bloquea y busca el canal de Mono por SDP; el anfitrión debe tener la sala abierta.
                val s = device.createRfcommSocketToServiceRecord(MONO_UUID)
                main.post { socket = s }
                s.connect()
                Log.i(LOG_TAG, "invitado: conectado con $peer")
                val read = guestLoop(guest, s.inputStream, s.outputStream) { m ->
                    if (m is Message.Snapshot) {
                        val names = m.state.players.map { it.name }
                        Log.i(LOG_TAG, "invitado: partida ${m.config.name}, ${m.config.squares.size} casillas, aquí ${m.seats.map(names::get)}")
                        main.post { join = Join.Connected(peer, m.config.name, m.seats.sorted().map(names::get)) }
                    }
                }
                Log.i(LOG_TAG, "invitado: fin de la sesión, $read mensajes")
                main.post { join = Join.Closed(peer, guest.closed) }
            } catch (e: IOException) {
                Log.i(LOG_TAG, "invitado: no se pudo con $peer: ${e.message}")
                main.post { join = Join.Closed(peer, e.message) }
            }
        }
    }
    when (val j = join) {
        Join.Choosing -> {
            Text(stringResource(R.string.join_choose), fontSize = 18.sp)
            paired.forEach { d -> BotonChiva(d.name ?: d.address, { connect(d) }, principal = false) }
            Text(stringResource(if (paired.isEmpty()) R.string.join_none else R.string.join_pair_hint), fontSize = 14.sp, color = Chiva.Tinta.copy(alpha = 0.7f))
        }
        is Join.Connecting -> Text(stringResource(R.string.join_connecting, j.peer), fontSize = 18.sp)
        is Join.Connected -> {
            Text(stringResource(R.string.host_connected, j.peer, j.board ?: "?"), fontSize = 18.sp)
            Text(stringResource(R.string.join_mine, j.mine.joinToString(", ")), fontSize = 16.sp)
        }
        is Join.Closed -> {
            Text(stringResource(R.string.join_closed, j.peer, j.reason ?: "—"), fontSize = 18.sp)
            BotonChiva(stringResource(R.string.join_again), { join = Join.Choosing })
        }
    }
}
