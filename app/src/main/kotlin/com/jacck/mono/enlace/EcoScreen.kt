package com.jacck.mono.enlace

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacck.mono.LOG_TAG
import com.jacck.mono.R

/** Líneas recibidas que se ven en pantalla; el total va aparte. */
private const val SHOWN = 12

/** Pantalla de la prueba de F5.1 (`--es enlace eco`): pide el permiso, enciende el Bluetooth y atiende al PC. */
@SuppressLint("MissingPermission") // `name` solo dentro de BluetoothGate, con el permiso concedido
@Composable
fun EcoScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.eco_title), style = MaterialTheme.typography.headlineSmall)
        BluetoothGate { adapter ->
            var event by remember { mutableStateOf<RfcommServer.Event?>(null) }
            var total by remember { mutableStateOf(0) }
            val lines = remember { mutableStateListOf<String>() }
            DisposableEffect(adapter) {
                val main = Handler(Looper.getMainLooper())
                val server = RfcommServer(adapter, { e ->
                    Log.i(LOG_TAG, "eco: $e")
                    main.post {
                        event = e
                        if (e is RfcommServer.Event.Connected) { total = 0; lines.clear() }
                    }
                }) { input, output ->
                    echoLoop(input, output) { n, line ->
                        Log.i(LOG_TAG, "eco: $n $line")
                        main.post {
                            total = n
                            lines.add(0, "$n: $line")
                            if (lines.size > SHOWN) lines.removeAt(lines.lastIndex)
                        }
                    }
                }
                server.start()
                onDispose { server.stop() }
            }
            Text(stringResource(R.string.eco_phone_name, adapter.name ?: "?"))
            Text(
                when (val e = event) {
                    null, RfcommServer.Event.Listening -> stringResource(R.string.eco_listening)
                    is RfcommServer.Event.Connected -> stringResource(R.string.eco_connected, e.peer)
                    is RfcommServer.Event.Closed -> stringResource(R.string.eco_closed, e.reason ?: "—")
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Text(stringResource(R.string.eco_received, total))
            lines.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
