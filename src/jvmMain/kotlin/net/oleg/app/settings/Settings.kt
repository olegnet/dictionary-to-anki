/*
 * Copyright 2022 Oleg Okhotnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalSerializationApi::class)

package net.oleg.app.settings

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger
import java.io.File

@Serializable
data class SettingsData(
    @SerialName("translationOrder") val translationOrder: String? = null,
    @SerialName("deckName") val deckName: String? = null,
    @SerialName("modelName") val modelName: String? = null,
)

class Settings private constructor(
    private val path: String,
    private var data: SettingsData
) {
    var translationOrder: String
        get() = data.translationOrder ?: "en-ru"
        set(value) = save(translationOrder = value)

    var deckName: String
        get() = data.deckName ?: "Default"
        set(value) = save(deckName = value)

    var modelName: String
        get() = data.modelName ?: "Basic"
        set(value) = save(modelName = value)

    private fun save(
        translationOrder: String? = data.translationOrder,
        deckName: String? = data.deckName,
        modelName: String? = data.modelName,
    ) {
        data = SettingsData(translationOrder, deckName, modelName)
        Json.encodeToStream(data, File(path).outputStream())
    }

    companion object {
        private const val SETTINGS_PATH = ".config/dict2anki/settings.json"

        private val logger = LoggerFactory.default.newLogger(Settings::class)

        fun load(overridePath: String? = null): Settings {
            val path = overridePath ?: "${System.getProperty("user.home")}/$SETTINGS_PATH"
            logger.debug { "path: $path" }

            val data = Json.decodeFromStream<SettingsData>(File(path).inputStream())

            return Settings(path, data)
        }
    }
}