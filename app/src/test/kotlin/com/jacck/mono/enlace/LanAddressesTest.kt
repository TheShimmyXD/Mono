package com.jacck.mono.enlace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LanAddressesTest {

    /** Interfaces del Redmi (`ip -br addr`, 2026-10-07) solo con datos móviles: ninguna es red local. */
    private val soloDatos = listOf("lo" to "127.0.0.1", "ccmni0" to "100.64.0.7", "ccmni2" to "203.0.113.9", "dummy0" to "fe80::2")

    @Test
    fun `solo con datos moviles no hay red local`() {
        assertEquals(emptyList<String>(), lanAddresses(soloDatos))
    }

    @Test
    fun `el Wi-Fi y el punto de acceso cuentan, sin repetir ni IPv6 ni autoasignadas`() {
        val found = soloDatos + listOf(
            "wlan0" to "10.0.1.50", "wlan0" to "fe80::1", "ap0" to "192.168.43.1", "wlan0" to "10.0.1.50",
            "rmnet_data0" to "10.0.0.2", "tun0" to "10.8.0.2", "wlan1" to "169.254.10.3",
        )
        assertEquals(listOf("10.0.1.50", "192.168.43.1"), lanAddresses(found))
    }
}
