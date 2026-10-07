package com.jacck.mono.engine.model

import kotlinx.serialization.json.Json

/**
 * JSON de tableros, presets, partidas y acciones (D-10). Estricto: una clave desconocida es
 * un error, para que un archivo mal escrito no se cargue a medias.
 */
object MonoJson {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    fun encodeConfig(config: GameConfig): String = json.encodeToString(config)

    fun decodeConfig(text: String): GameConfig = json.decodeFromString(text)

    fun encodeState(state: GameState): String = json.encodeToString(state)

    fun decodeState(text: String): GameState = json.decodeFromString(text)

    fun encodeSaved(saved: SavedGame): String = json.encodeToString(saved)

    fun decodeSaved(text: String): SavedGame = json.decodeFromString(text)

    fun encodeAction(action: Action): String = json.encodeToString(action)

    fun decodeAction(text: String): Action = json.decodeFromString(text)
}
