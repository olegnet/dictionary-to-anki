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

package net.oleg.app.dictionary

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.log.Logger
import org.kodein.log.LoggerFactory

typealias Languages = List<String>

class Dictionary(
    private val client: HttpClient,
    private val dictionaryKey: String,
) {
    companion object {
        private const val HOST = "dictionary.yandex.net"
        private val PROTOCOL = URLProtocol.HTTPS
        private val PATH = listOf("api", "v1", "dicservice.json")
        private val GET_LANGS_PARAMS = PATH.plus("getLangs")
        private val LOOKUP_PARAMS = PATH.plus("lookup")
        private const val KEY = "key"
        private const val LANG = "lang"
        private const val TEXT = "text"
    }

    private val logger = LoggerFactory.default.newLogger(Logger.Tag(Dictionary::class))

    suspend fun getLanguages(): RequestState<Languages> =
        withContext(Dispatchers.IO) {
            val url = URLBuilder(
                protocol = PROTOCOL,
                host = HOST,
                pathSegments = GET_LANGS_PARAMS,
                parameters = parametersOf("key", dictionaryKey)
            ).build()
            logUrl(url)

            val response: HttpResponse = client.get(url)
            logger.debug { "status: ${response.status}" }

            result(response)
        }

    suspend fun lookup(lang: String, text: String): RequestState<Lookup> =
        withContext(Dispatchers.IO) {
            val url = URLBuilder(
                protocol = PROTOCOL,
                host = HOST,
                pathSegments = LOOKUP_PARAMS,
                parameters = parametersOf("key", dictionaryKey)
                    .plus(parametersOf(LANG, lang))
                    .plus(parametersOf(TEXT, text))
            ).build()
            logUrl(url)

            val response: HttpResponse = client.get(url)
            logger.debug { "status: ${response.status}" }

            result(response)
        }

    private fun logUrl(url: Url) =
        logger.debug { "url: $url".replace(KEY, "…") }

    private suspend inline fun <reified T> result(response: HttpResponse): RequestState<T> =
        when (response.status) {
            HttpStatusCode.OK ->
                RequestState.Success(response.body())
            else ->
                RequestState.Failure(DictionaryError.from(response.status))
        }
}