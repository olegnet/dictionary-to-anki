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

package net.oleg.app.anki

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

typealias ItemNamesList = List<String>

@Suppress("EnumEntryName")
enum class ItemNames {
    modelNames,
    deckNames
}

class Anki(
    private val client: HttpClient,
    private val apiKey: String?,
) {
    companion object {
        const val ANKI_CONNECT_URL = "http://127.0.0.1:8765"
        const val API_VERSION = 6
        const val ACTION_REQUEST_PERMISSION = "requestPermission"
        const val ACTION_ADD_NOTE = "addNote"
        const val PERMISSION_GRANTED = "granted"
    }

    private val url: Url = URLBuilder(ANKI_CONNECT_URL).build()

    private val logger = LoggerFactory.default.newLogger(Anki::class)

    suspend fun requestPermission(): AnkiResponse<Any?> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request(url) {
                    contentType(ContentType.Application.Json)
                    setBody(Request(action = ACTION_REQUEST_PERMISSION, version = API_VERSION))
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val responseJson: Response<Permission?> = response.body()
                        logger.debug { "json: $responseJson" }

                        if (responseJson.error.isNullOrEmpty() &&
                            responseJson.result?.permission == PERMISSION_GRANTED &&
                            responseJson.result.version >= API_VERSION
                        ) {
                            AnkiResponse.Result(null)
                        } else {
                            AnkiResponse.Error(error = "Something wrong with Anki Connect response")
                        }
                    }
                    else ->
                        AnkiResponse.Error(error = "HTTP Error code ${response.status}")
                }
            } catch (ex: Exception) {
                logger.error { "requestPermission: $ex" }
                AnkiResponse.Error(error = ex.message ?: ex.stackTraceToString())
            }
        }

    private suspend inline fun <reified T> processAnkiResponse(httpResponse: HttpResponse): AnkiResponse<T> =
        when (httpResponse.status) {
            HttpStatusCode.OK -> {
                val response: Response<T> = httpResponse.body()
                logger.debug { "json: $response" }
                when {
                    response.result != null ->
                        AnkiResponse.Result(result = response.result)
                    response.error != null ->
                        AnkiResponse.Error(error = response.error)
                    else ->
                        throw RuntimeException("Unknown error: $response")
                }
            }
            else -> {
                logger.debug { "http code: ${httpResponse.status}" }
                AnkiResponse.Error(error = "HTTP Error code ${httpResponse.status}")
            }
        }

    suspend fun addNote(deckName: String, modelName: String, front: String, back: String): AnkiResponse<Long?> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request(url) {
                    contentType(ContentType.Application.Json)
                    setBody(
                        Request(
                            apiKey = apiKey,
                            action = ACTION_ADD_NOTE,
                            version = API_VERSION,
                            params = Params(
                                note = Note(
                                    deckName = deckName,
                                    modelName = modelName,
                                    fields = Fields(
                                        front = front,
                                        back = back
                                    )
                                )
                            )
                        )
                    )
                }
                processAnkiResponse(response)
            } catch (ex: Exception) {
                logger.error { "addNote: $ex" }
                AnkiResponse.Error(error = ex.message ?: ex.stackTraceToString())
            }
        }

    suspend fun getNames(action: String): AnkiResponse<ItemNamesList> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request(url) {
                    contentType(ContentType.Application.Json)
                    setBody(Request(apiKey = apiKey, action = action, version = API_VERSION))
                }
                processAnkiResponse(response)
            } catch (ex: Exception) {
                logger.error { "getNames: $ex" }
                AnkiResponse.Error(error = ex.message ?: ex.stackTraceToString())
            }
        }
}