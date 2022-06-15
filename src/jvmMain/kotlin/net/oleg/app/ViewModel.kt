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
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.log.Logger
import org.kodein.log.LoggerFactory

typealias Languages = List<String>

@Serializable
data class Translations(
    val text: String,
    val pos: String,
    val gen: String? = null,
    val fr: Int? = null,
    //  syn     Array of synonyms
    //  mean    Array of meanings
    //  ex	    Array of examples
)

@Serializable
data class DictionaryEntries(
    val text: String,
    val pos: String,
    val ts: String? = null, // transcription
    val tr: List<Translations>,
)

@Serializable
data class Lookup(
    val def: List<DictionaryEntries>,
)

class ViewModel(
    private val client: HttpClient,
) {
    companion object {
        // Get one from https://yandex.com/dev/dictionary/
        private const val KEY = YANDEX_API_KEY

        private const val HOST = "dictionary.yandex.net"
        private val PROTOCOL = URLProtocol.HTTPS
        private val PATH = listOf("api", "v1", "dicservice.json")
        private val GET_LANGS_PARAMS = PATH.plus("getLangs")
        private val LOOKUP_PARAMS = PATH.plus("lookup")
        private val KEY_PARAM = parametersOf("key", KEY)
        private const val LANG = "lang"
        private const val TEXT = "text"
    }

    private val logger = LoggerFactory.default.newLogger(Logger.Tag(ViewModel::class))

    suspend fun getLanguages(): Languages =
        withContext(Dispatchers.IO) {
            val url = URLBuilder(
                protocol = PROTOCOL,
                host = HOST,
                pathSegments = GET_LANGS_PARAMS,
                parameters = KEY_PARAM
            ).build()
            logger.debug { "url: $url" }

            val response: HttpResponse = client.get(url)
            logger.debug { "status: ${response.status}" }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val languages: Languages = response.body()
                    logger.debug { "languages: $languages" }
                    return@withContext languages
                }
                else -> {
                    // FIXME
                    // HttpStatusCode.Unauthorized      Invalid API key
                    // HttpStatusCode.PaymentRequired   This API key has been blocked
                    listOf()
                }
            }
        }

    suspend fun lookup(lang: String, text: String): Lookup =
        withContext(Dispatchers.IO) {
            val url = URLBuilder(
                protocol = PROTOCOL,
                host = HOST,
                pathSegments = LOOKUP_PARAMS,
                parameters = KEY_PARAM
                    .plus(parametersOf(LANG, lang))
                    .plus(parametersOf(TEXT, text))
            ).build()
            logger.debug { "url: $url" }

            val response: HttpResponse = client.get(url)
            logger.debug { "status: ${response.status}" }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val languages: Lookup = response.body()
                    logger.debug { "languages: $languages" }
                    return@withContext languages
                }
                else -> {
                    // FIXME
                    // 401 Invalid API key
                    // 402 This API key has been blocked
                    // 403 Exceeded the daily limit on the number of requests
                    // 413 The text size exceeds the maximum
                    // 501 The specified translation direction is not supported
                    Lookup(listOf())
                }
            }
        }
}