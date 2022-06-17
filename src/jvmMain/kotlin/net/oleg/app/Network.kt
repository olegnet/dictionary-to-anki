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

package net.oleg.app

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.kodein.log.Logger.Tag
import org.kodein.log.LoggerFactory

object Network {
    val kodeinLogger = LoggerFactory.default.newLogger(Tag(Network::class))

    fun client(enableLogging: Boolean = false): HttpClient {
        return HttpClient(CIO) {
            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            kodeinLogger.debug { message }
                        }
                    }
                    level = LogLevel.ALL
                }
            }
            install(ContentNegotiation) {
                developmentMode = enableLogging
                json(
                    json = Json {
                        prettyPrint = enableLogging
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                    // for Anki
                    contentType = ContentType("*", "json")
                )
            }
        }
    }
}