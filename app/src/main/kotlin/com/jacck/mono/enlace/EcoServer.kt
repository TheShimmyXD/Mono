package com.jacck.mono.enlace

import java.io.InputStream
import java.io.OutputStream

/**
 * Prueba de concepto de F5.1 (D-45): el teléfono escucha por RFCOMM ([RfcommServer]) y devuelve
 * cada línea que recibe; el PC se conecta con `pc/eco.py`.
 */

/** Primera línea que manda el teléfono al conectar; el PC la usa para saber que llegó a Mono. */
const val GREETING = "MONO eco 1"

/**
 * Manda [GREETING] y responde `eco <n>: <línea>` a cada línea de [input] hasta que el otro lado
 * cierra. Devuelve cuántas líneas contestó. No sabe de Bluetooth, para probarlo en la JVM.
 */
fun echoLoop(input: InputStream, output: OutputStream, onMessage: (Int, String) -> Unit = { _, _ -> }): Int {
    val reader = input.bufferedReader(Charsets.UTF_8)
    val writer = output.bufferedWriter(Charsets.UTF_8)
    writer.write("$GREETING\n")
    writer.flush()
    var n = 0
    while (true) {
        val line = reader.readLine() ?: break
        n++
        writer.write("eco $n: $line\n")
        writer.flush()
        onMessage(n, line)
    }
    return n
}
