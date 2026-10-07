package com.jacck.mono.enlace

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class EchoLoopTest {

    @Test
    fun `saluda y devuelve cada linea numerada hasta que el otro lado cierra`() {
        val out = ByteArrayOutputStream()
        val seen = mutableListOf<Pair<Int, String>>()
        val n = echoLoop(ByteArrayInputStream("hola\n¿Pagaste el alquiler?\n".toByteArray()), out) { i, s -> seen += i to s }
        assertEquals(2, n)
        assertEquals(listOf(1 to "hola", 2 to "¿Pagaste el alquiler?"), seen)
        assertEquals("$GREETING\neco 1: hola\neco 2: ¿Pagaste el alquiler?\n", out.toString(Charsets.UTF_8))
    }

    @Test
    fun `sin mensajes solo saluda`() {
        val out = ByteArrayOutputStream()
        assertEquals(0, echoLoop(ByteArrayInputStream(ByteArray(0)), out))
        assertEquals("$GREETING\n", out.toString(Charsets.UTF_8))
    }
}
