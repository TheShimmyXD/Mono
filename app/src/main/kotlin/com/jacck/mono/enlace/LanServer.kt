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
import kotlin.concurrent.thread

/** Tipo de servicio DNS-SD con el que se anuncia la sala: el invitado (y `pc/invitado.py`) lo buscan. */
const val SERVICE_TYPE = "_mono._tcp"

/**
 * Servidor del enlace por la red local (F5.3, D-48): TCP en un puerto libre de todas las
 * interfaces (Wi-Fi o el punto de acceso del teléfono) y anunciado con NSD (mDNS) como [label],
 * para que el invitado lo encuentre sin escribir la dirección. Atiende a un invitado a la vez con
 * [session] y vuelve a esperar, hasta [stop]. `accept` y `readLine` bloquean: hilo propio.
 */
class LanServer(
    private val context: Context,
    private val label: String,
    private val onEvent: (LinkEvent) -> Unit,
    private val session: (InputStream, OutputStream) -> Unit,
) {
    @Volatile private var server: ServerSocket? = null
    @Volatile private var socket: Socket? = null
    @Volatile private var stopped = false
    private var registration: NsdManager.RegistrationListener? = null
    private val nsd: NsdManager? get() = context.getSystemService(NsdManager::class.java)

    fun start() = thread(name = "mono-lan") {
        val listening = try { ServerSocket(0) } catch (e: IOException) { onEvent(LinkEvent.Closed(e.message)); return@thread }
        server = listening
        advertise(listening.localPort)
        while (!stopped) {
            try {
                onEvent(LinkEvent.Listening(listening.localPort))
                val s = listening.accept()
                s.tcpNoDelay = true // mensajes cortos: que no esperen a juntarse
                socket = s
                onEvent(LinkEvent.Connected(s.inetAddress.hostAddress ?: "?"))
                session(s.getInputStream(), s.getOutputStream())
                onEvent(LinkEvent.Closed(null))
            } catch (e: IOException) {
                if (stopped) break
                onEvent(LinkEvent.Closed(e.message))
                Thread.sleep(2000)
            } finally {
                socket?.close()
                socket = null
            }
        }
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
        socket?.close()
    }
}
