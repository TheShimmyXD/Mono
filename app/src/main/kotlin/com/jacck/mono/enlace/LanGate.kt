package com.jacck.mono.enlace

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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.jacck.mono.BotonChiva
import com.jacck.mono.LOG_TAG
import com.jacck.mono.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Desde Android 17 (API 37) hablar con la red local pide «Dispositivos cercanos»
 * (`ACCESS_LOCAL_NETWORK`); anunciar un servicio por NSD lo exige siempre (`NsdManager.java` del SDK 37).
 */
private const val LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
private const val API_LOCAL_NETWORK = 37

/** Prefijos de interfaces que no son la red local: bucle, datos móviles (`ccmni` en MediaTek, `rmnet` en Qualcomm), VPN y virtuales. */
private val NOT_LAN = listOf("lo", "dummy", "ccmni", "rmnet", "v4-", "clat", "tun", "ipsec", "ifb")

/** Direcciones IPv4 de la red local entre [found] (nombre de interfaz, dirección). */
fun lanAddresses(found: List<Pair<String, String>>): List<String> = found
    .filter { (name, ip) -> NOT_LAN.none(name::startsWith) && ':' !in ip && !ip.startsWith("169.254.") }
    .map { it.second }.distinct()

private fun currentAddresses(): List<String> = lanAddresses(
    NetworkInterface.getNetworkInterfaces()?.toList().orEmpty().filter { it.isUp }
        .flatMap { i -> i.inetAddresses.toList().filterIsInstance<Inet4Address>().map { i.name to (it.hostAddress ?: "") } },
)

/**
 * Pide el permiso de la red local donde hace falta y espera a que el teléfono esté en un Wi-Fi o
 * tenga el punto de acceso encendido (lo revisa cada 2 s); entonces dibuja [content] con sus direcciones.
 */
@Composable
fun LanGate(content: @Composable (List<String>) -> Unit) {
    val context = LocalContext.current
    val needs = Build.VERSION.SDK_INT >= API_LOCAL_NETWORK
    var granted by remember { mutableStateOf(!needs || context.checkSelfPermission(LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
        Log.i(LOG_TAG, "red: permiso ACCESS_LOCAL_NETWORK ${if (it) "concedido" else "negado"}")
    }
    LaunchedEffect(Unit) { if (!granted) ask.launch(LOCAL_NETWORK) }
    val addresses by produceState(emptyList<String>()) {
        while (true) {
            val now = withContext(Dispatchers.IO) { currentAddresses() }
            if (now != value) Log.i(LOG_TAG, "red: direcciones locales $now")
            value = now
            delay(2000)
        }
    }
    when {
        !granted -> {
            Text(stringResource(R.string.lan_why_permission), fontSize = 18.sp)
            BotonChiva(stringResource(R.string.lan_grant), { ask.launch(LOCAL_NETWORK) })
        }
        addresses.isEmpty() -> Text(stringResource(R.string.lan_none), fontSize = 18.sp)
        else -> content(addresses)
    }
}
