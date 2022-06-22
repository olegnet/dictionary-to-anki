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
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

@Serializable
data class Request(
    @SerialName("key") val apiKey: String? = null,
    @SerialName("action") val action: String,
    @SerialName("version") val version: Int,
    @SerialName("params") val params: Params? = null,
)

@Serializable
data class Params(
    @SerialName("note") val note: Note,
)

@Serializable
data class Note(
    @SerialName("deckName") val deckName: String,
    @SerialName("modelName") val modelName: String,
    @SerialName("fields") val fields: Fields,
)

@Serializable
data class Fields(
    @SerialName("Front") val front: String,
    @SerialName("Back") val back: String,
)

@Serializable
data class Response<T>(
    @SerialName("result") val result: T? = null,
    @SerialName("error") val error: String? = null,
)

@Serializable
data class Permission(
    @SerialName("permission") val permission: String,
    @SerialName("requireApikey") val requireApiKey: Boolean,
    @SerialName("version") val version: Int,
)

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
    }

    private val url: Url = URLBuilder(ANKI_CONNECT_URL).build()

    private val logger = LoggerFactory.default.newLogger(Anki::class)

    suspend fun requestPermission(): AnkiResponseState<Any?> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request(url) {
                    contentType(ContentType.Application.Json)
                    setBody(Request(action = ACTION_REQUEST_PERMISSION, version = API_VERSION))
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val responseJson: Response<Permission?> = response.body()
                        logger.debug { "responseJson: $responseJson" }

                        if (responseJson.error.isNullOrEmpty() &&
                            responseJson.result?.permission == "granted" &&
                            responseJson.result.version >= 6) {

                            AnkiResponseState.Result(null)
                        } else {
                            AnkiResponseState.Error(error = "Something wrong with Anki Connect response")
                        }
                    }
                    else ->
                        AnkiResponseState.Error(error = "HTTP Error code ${response.status}")
                }
            } catch (ex: Exception) {
                logger.error { "requestPermission: $ex" }
                AnkiResponseState.Error(error = ex.message ?: ex.stackTraceToString())
            }
        }

    suspend fun addNote(deckName: String, modelName: String, front: String, back: String): Response<Long?> =
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
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val responseJson: Response<Long?> = response.body()
                        logger.debug { "responseJson: $responseJson" }

                        responseJson
                    }
                    else ->
                        Response(error = "HTTP Error code ${response.status}")
                }
            } catch (ex: Exception) {
                logger.error { "addNote: $ex" }
                Response(error = ex.message)
            }
        }

    suspend fun getNames(action: String): Response<ItemNamesList> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request(url) {
                    contentType(ContentType.Application.Json)
                    setBody(Request(apiKey = apiKey, action = action, version = API_VERSION))
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val responseJson: Response<ItemNamesList> = response.body()
                        logger.debug { "responseJson: $responseJson" }

                        responseJson
                    }
                    else ->
                        Response(error = "HTTP Error code ${response.status}")
                }
            } catch (ex: Exception) {
                logger.error { "getNames: $ex" }
                Response(error = ex.message)
            }
        }
}