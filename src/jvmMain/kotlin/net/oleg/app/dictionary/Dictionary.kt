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
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getLanguages(): DictionaryResponse<Languages> =
        withContext(Dispatchers.IO) {
            val url = URLBuilder(
                protocol = PROTOCOL,
                host = HOST,
                pathSegments = GET_LANGS_PARAMS,
                parameters = parametersOf(KEY, dictionaryKey)
            ).build()
            get(url)
        }

    suspend fun lookup(lang: String, text: String): DictionaryResponse<Lookup> =
        withContext(Dispatchers.IO) {
            val url = URLBuilder(
                protocol = PROTOCOL,
                host = HOST,
                pathSegments = LOOKUP_PARAMS,
                parameters = parametersOf(KEY, dictionaryKey)
                    .plus(parametersOf(LANG, lang))
                    .plus(parametersOf(TEXT, text))
            ).build()
            get(url)
        }

    private suspend inline fun <reified T> get(url: Url): DictionaryResponse<T> {
        return try {
            val response = client.get(url)
            when (response.status) {
                HttpStatusCode.OK -> {
                    DictionaryResponse.Result(response.body())
                }
                else -> {
                    val dictionaryError: DictionaryError = response.body()
                    DictionaryResponse.Error(dictionaryError.message ?: "Http error code ${response.status}")
                }
            }
        } catch (ex: Exception) {
            DictionaryResponse.Error(ex.message ?: "Unknown error")
        }
    }
}