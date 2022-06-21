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
import java.util.*

class Keys private constructor(
    // Get one from https://yandex.com/dev/dictionary/
    val dictionary: String?,
    // Set one in Anki settings (optional)
    val anki: String?,
) {
    companion object {
        private val logger = LoggerFactory.default.newLogger(Keys::class)

        fun load(overridePath: String? = null): Keys {
            val properties = loadProperties(overridePath)
            return Keys(
                dictionary = properties.getProperty("dictionary"),
                anki = properties.getProperty("anki")
            )
        }

        private fun loadProperties(overridePath: String?): Properties {
            val path = overridePath ?: "${System.getProperty("user.home")}/.config/dict2anki/keys.properties"
            val file = File(path)
            logger.debug { "file: $file" }

            val properties = Properties()
            properties.load(file.inputStream())
            logger.debug { "properties found: " + properties.keys.joinToString(separator = ", ") }

            return properties
        }
    }
}