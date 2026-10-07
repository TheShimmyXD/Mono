package com.jacck.mono.game

import com.jacck.mono.engine.model.MonoJson
import com.jacck.mono.engine.model.SavedGame
import com.jacck.mono.engine.model.TurnPhase
import java.io.File

/**
 * La partida guardada (F3.6, D-26): un solo `partida.json` en la carpeta privada de la app. Se
 * escribe tras cada jugada, primero a un temporal y luego se renombra, para que cerrar la app a
 * mitad de escritura no deje un archivo cortado. Una partida terminada se borra.
 */
class SaveFile(dir: File) {

    private val file = File(dir, "partida.json")
    private val temp = File(dir, "partida.json.tmp")

    /** La partida a medias, o null si no hay o el archivo no se puede leer. */
    fun read(): SavedGame? {
        if (!file.exists()) return null
        return try {
            MonoJson.decodeSaved(file.readText()).takeIf { it.state.phase !is TurnPhase.Over }
        } catch (_: IllegalArgumentException) {
            null // SerializationException es un IllegalArgumentException: JSON viejo o dañado.
        }
    }

    fun write(saved: SavedGame) {
        if (saved.state.phase is TurnPhase.Over) {
            file.delete()
            return
        }
        temp.writeText(MonoJson.encodeSaved(saved))
        check(temp.renameTo(file)) { "no se pudo guardar la partida en $file" }
    }
}
