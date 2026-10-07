package com.jacck.mono.enlace

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.LOG_TAG
import com.jacck.mono.R

/** En Android 12+ (API 31) RFCOMM pide «Dispositivos cercanos»; antes bastaba el permiso BLUETOOTH del manifiesto. */
private fun hasConnectPermission(context: Context) = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
    context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED

/**
 * Pide el permiso `BLUETOOTH_CONNECT` (diciendo para qué) y que se encienda el Bluetooth; con los
 * dos, dibuja [content] con el adaptador. Lo usan la prueba del eco (F5.1) y la sala (F5.3).
 */
@Composable
fun BluetoothGate(content: @Composable (BluetoothAdapter) -> Unit) {
    val context = LocalContext.current
    val adapter: BluetoothAdapter? = remember { context.getSystemService(BluetoothManager::class.java)?.adapter }
    var granted by remember { mutableStateOf(hasConnectPermission(context)) }
    var enabled by remember { mutableStateOf(adapter?.isEnabled == true) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        Log.i(LOG_TAG, "bluetooth: permiso BLUETOOTH_CONNECT ${if (it) "concedido" else "negado"}")
    }
    val turnOn = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        enabled = adapter?.isEnabled == true
        Log.i(LOG_TAG, "bluetooth: ${if (enabled) "encendido" else "sigue apagado"}")
    }
    LaunchedEffect(Unit) { if (!granted) ask.launch(Manifest.permission.BLUETOOTH_CONNECT) }
    when {
        adapter == null -> Text(stringResource(R.string.bt_none), fontSize = 18.sp)
        !granted -> {
            Text(stringResource(R.string.bt_why_permission), fontSize = 18.sp)
            BotonChiva(stringResource(R.string.bt_grant), { ask.launch(Manifest.permission.BLUETOOTH_CONNECT) })
        }
        !enabled -> {
            Text(stringResource(R.string.bt_off), fontSize = 18.sp)
            // ACTION_REQUEST_ENABLE pide BLUETOOTH_CONNECT, que aquí ya está concedido.
            BotonChiva(stringResource(R.string.bt_turn_on), { turnOn.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) })
        }
        else -> content(adapter)
    }
}
