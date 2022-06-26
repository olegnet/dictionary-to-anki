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

package net.oleg.app.settings

import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger
import java.io.File
import java.io.IOException
import java.util.*

class Keys private constructor(
    // Get one from https://yandex.com/dev/dictionary/
    val dictionary: String?,
    // Set one in Anki settings (optional)
    val anki: String?,
) {
    companion object {
        private const val KEYS_PATH = ".config/dict2anki/keys.properties"
        private const val ANKI_KEY = "anki"
        const val DICTIONARY_KEY = "dictionary"
        const val DICTIONARY_API_URL = "https://yandex.com/dev/dictionary/"

        val defaultPath = "${System.getProperty("user.home")}/$KEYS_PATH"

        private val logger = LoggerFactory.default.newLogger(Keys::class)

        fun createPropertyFile() {
            val file = File(defaultPath)
            if (file.exists()) {
                return
            }
            file.parentFile.mkdirs()
            file.writeText("# Enter your keys here\n$DICTIONARY_KEY=\n$ANKI_KEY=\n")
        }

        fun load(overridePath: String? = null): Keys {
            val properties = loadProperties(overridePath)
            return Keys(
                dictionary = properties.getProperty(DICTIONARY_KEY),
                anki = properties.getProperty(ANKI_KEY)
            )
        }

        private fun loadProperties(overridePath: String?): Properties {
            val path = overridePath ?: defaultPath
            logger.debug { "path: $path" }

            val properties = Properties()
            try {
                properties.load(File(path).inputStream())
                logger.debug { "properties found: " + properties.keys.joinToString(separator = ", ") }
            } catch (ex: IOException) {
                logger.error(ex)
            }

            return properties
        }
    }
}