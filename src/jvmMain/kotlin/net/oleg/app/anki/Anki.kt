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
    @SerialName("result") val result: T?,
    @SerialName("error") val error: String?,
)

@Serializable
data class Permission(
    @SerialName("permission") val permission: String,
    @SerialName("requireApikey") val requireApiKey: Boolean,
    @SerialName("version") val version: Int,
)

class Anki(
    private val client: HttpClient,
    private val apiKey: String?,
) {
    private val url: Url = URLBuilder("http://127.0.0.1:8765").build()

    private val logger = LoggerFactory.default.newLogger(Anki::class)

    suspend fun requestPermission(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request(url) {
                    contentType(ContentType.Application.Json)
                    setBody(Request(action = "requestPermission", version = 6))
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val responseJson: Response<Permission?> = response.body()
                        logger.debug { "responseJson: $responseJson" }

                        responseJson.error.isNullOrEmpty() &&
                                responseJson.result?.permission == "granted" &&
                                responseJson.result.version >= 6
                        // FIXME check responseJson.result.requireApiKey
                    }
                    else ->
                        false
                }
            } catch (ex: Exception) {
                logger.error { "requestPermission: $ex" }
                // FIXME show "connection refused" message to the user
                false
            }
        }

    suspend fun addNote(deckName: String, modelName: String, front: String, back: String) =
        withContext(Dispatchers.IO) {
            val response = client.request(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    Request(
                        apiKey = apiKey,
                        action = "addNote",
                        version = 6,
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
                }
                else -> {
                }
            }
        }
}