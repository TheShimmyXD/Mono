package com.jacck.mono.enlace

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jacck.mono.LOG_TAG
import com.jacck.mono.R

/** Líneas recibidas que se ven en pantalla; el total va aparte. */
private const val SHOWN = 12

/** En Android 12+ (API 31) RFCOMM pide «Dispositivos cercanos»; antes bastaba el permiso BLUETOOTH del manifiesto. */
private fun hasConnectPermission(context: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
    context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

/** Pantalla de la prueba de F5.1 (`--es enlace eco`): pide el permiso, enciende el Bluetooth y atiende al PC. */
@SuppressLint("MissingPermission") // `name` y ACTION_REQUEST_ENABLE solo con `granted`
@Composable
fun EcoScreen() {
    val context = LocalContext.current
    val adapter: BluetoothAdapter? = remember { context.getSystemService(BluetoothManager::class.java)?.adapter }
    var granted by remember { mutableStateOf(hasConnectPermission(context)) }
    var enabled by remember { mutableStateOf(adapter?.isEnabled == true) }
    var event by remember { mutableStateOf<EcoServer.Event?>(null) }
    var total by remember { mutableStateOf(0) }
    val lines = remember { mutableStateListOf<String>() }

    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        Log.i(LOG_TAG, "eco: permiso BLUETOOTH_CONNECT ${if (it) "concedido" else "negado"}")
    }
    val turnOn = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        enabled = adapter?.isEnabled == true
    }
    LaunchedEffect(Unit) { if (!granted) ask.launch(Manifest.permission.BLUETOOTH_CONNECT) }

    DisposableEffect(granted, enabled) {
        if (adapter == null || !granted || !enabled) return@DisposableEffect onDispose { }
        val main = Handler(Looper.getMainLooper())
        val server = EcoServer(adapter) { e ->
            Log.i(LOG_TAG, "eco: $e")
            main.post {
                if (e is EcoServer.Event.Message) {
                    total = e.n
                    lines.add(0, "${e.n}: ${e.text}")
                    if (lines.size > SHOWN) lines.removeAt(lines.lastIndex)
                } else {
                    event = e
                    if (e is EcoServer.Event.Connected) { total = 0; lines.clear() }
                }
            }
        }
        server.start()
        onDispose { server.stop() }
    }

    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.eco_title), style = MaterialTheme.typography.headlineSmall)
        when {
            adapter == null -> Text(stringResource(R.string.eco_no_bluetooth))
            !granted -> {
                Text(stringResource(R.string.eco_why_permission))
                Button(onClick = { ask.launch(Manifest.permission.BLUETOOTH_CONNECT) }) { Text(stringResource(R.string.eco_grant)) }
            }
            !enabled -> {
                Text(stringResource(R.string.eco_bluetooth_off))
                Button(onClick = { turnOn.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }) { Text(stringResource(R.string.eco_turn_on)) }
            }
            else -> {
                Text(stringResource(R.string.eco_phone_name, adapter.name ?: "?"))
                Text(
                    when (val e = event) {
                        null, EcoServer.Event.Listening -> stringResource(R.string.eco_listening)
                        is EcoServer.Event.Connected -> stringResource(R.string.eco_connected, e.peer)
                        is EcoServer.Event.Closed -> stringResource(R.string.eco_closed, e.reason ?: "—")
                        is EcoServer.Event.Message -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(stringResource(R.string.eco_received, total))
                lines.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
