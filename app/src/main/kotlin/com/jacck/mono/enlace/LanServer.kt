package com.jacck.mono.enlace

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.jacck.mono.LOG_TAG
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/** Tipo de servicio DNS-SD con el que se anuncia la sala: el invitado (y `pc/invitado.py`) lo buscan. */
const val SERVICE_TYPE = "_mono._tcp"

/**
 * Silencio máximo del invitado antes de dar la conexión por perdida (F5.5, D-74): el invitado
 * avisa al menos cada 5-10 s (`Resync` o latido), así que 30 s callado es una red caída.
 */
const val GUEST_SILENCE_MS = 30_000

/**
 * Servidor del enlace por la red local (F5.3, D-48): TCP en un puerto libre de todas las
 * interfaces (Wi-Fi o el punto de acceso del teléfono) y anunciado con NSD (mDNS) como [label],
 * para que el invitado lo encuentre sin escribir la dirección. Atiende con [session] a un invitado
 * a la vez ([acceptLoop]), hasta [stop]. `accept` y `readLine` bloquean: hilos propios.
 */
class LanServer(
    private val context: Context,
    private val label: String,
    private val onEvent: (LinkEvent) -> Unit,
    private val session: (InputStream, OutputStream) -> Unit,
) {
    @Volatile private var server: ServerSocket? = null
    private val socket = AtomicReference<Socket?>(null)
    @Volatile private var stopped = false
    private var registration: NsdManager.RegistrationListener? = null
    private val nsd: NsdManager? get() = context.getSystemService(NsdManager::class.java)

    fun start() = thread(name = "mono-lan") {
        val listening = try { ServerSocket(0) } catch (e: IOException) { onEvent(LinkEvent.Closed(e.message)); return@thread }
        server = listening
        advertise(listening.localPort)
        acceptLoop(listening, socket, { stopped }, onEvent, session)
    }

    /** Anuncia la sala por mDNS; si falla, el invitado aún puede escribir la dirección que muestra la sala. */
    private fun advertise(port: Int) {
        val info = NsdServiceInfo().apply { serviceName = label; serviceType = SERVICE_TYPE; setPort(port) }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(i: NsdServiceInfo) { Log.i(LOG_TAG, "red: anunciada como «${i.serviceName}» $SERVICE_TYPE, puerto $port") }
            override fun onRegistrationFailed(i: NsdServiceInfo, code: Int) { Log.i(LOG_TAG, "red: no se pudo anunciar (código $code)") }
            override fun onServiceUnregistered(i: NsdServiceInfo) { Log.i(LOG_TAG, "red: anuncio retirado") }
            override fun onUnregistrationFailed(i: NsdServiceInfo, code: Int) {}
        }
        registration = listener
        nsd?.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener)
    }

    fun stop() {
        stopped = true
        registration?.let { r -> try { nsd?.unregisterService(r) } catch (_: IllegalArgumentException) {} }
        server?.close()
        socket.getAndSet(null)?.close()
    }
}

/**
 * Atiende invitados en [listening] hasta que [stopped] (F5.3, F5.5, D-74). Cada conexión tiene su
 * hilo con [session]; la que llega nueva reemplaza a la anterior y la cierra, porque tras un corte
 * de red la vieja puede seguir abierta sin saberlo. Con [silenceMs] sin leer nada, la sesión se
 * corta sola. Solo la conexión vigente ([current]) cuenta su fin a [onEvent]. Sin Android, para la JVM.
 */
fun acceptLoop(
    listening: ServerSocket,
    current: AtomicReference<Socket?>,
    stopped: () -> Boolean,
    onEvent: (LinkEvent) -> Unit,
    session: (InputStream, OutputStream) -> Unit,
    silenceMs: Int = GUEST_SILENCE_MS,
) {
    onEvent(LinkEvent.Listening(listening.localPort))
    while (!stopped()) {
        val s = try {
            listening.accept()
        } catch (e: IOException) {
            if (stopped()) break
            onEvent(LinkEvent.Closed(e.message))
            Thread.sleep(2000)
            continue
        }
        s.tcpNoDelay = true // mensajes cortos: que no esperen a juntarse
        s.soTimeout = silenceMs
        val old = current.getAndSet(s)
        old?.close()
        onEvent(LinkEvent.Connected(s.inetAddress.hostAddress ?: "?", replaced = old != null))
        thread(name = "mono-sesion") {
            val reason = try {
                session(s.getInputStream(), s.getOutputStream())
                null
            } catch (e: IOException) {
                e.message ?: e.javaClass.simpleName
            } finally {
                s.close()
            }
            if (current.compareAndSet(s, null)) {
                onEvent(LinkEvent.Closed(reason))
                if (!stopped()) onEvent(LinkEvent.Listening(listening.localPort))
            }
        }
    }
}
