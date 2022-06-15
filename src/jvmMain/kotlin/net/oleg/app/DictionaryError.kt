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

import io.ktor.http.*

class DictionaryError private constructor(val code: Int, message: String) : Exception(message) {
    companion object {
        fun from(httpStatusCode: HttpStatusCode): DictionaryError =
            when (httpStatusCode.value) {
                400 -> DictionaryError(httpStatusCode.value, httpStatusCode.description)    // FIXME "Bad request"
                401 -> DictionaryError(httpStatusCode.value, "Invalid API key")
                402 -> DictionaryError(httpStatusCode.value, "API key has been blocked")
                403 -> DictionaryError(httpStatusCode.value, "Exceeded the daily limit of requests")
                413 -> DictionaryError(httpStatusCode.value, "Text size exceeded")
                501 -> DictionaryError(httpStatusCode.value, "Translation direction is not supported")
                else -> DictionaryError(httpStatusCode.value,
                    "Unknown error ${httpStatusCode.value}: ${httpStatusCode.description}")
            }
    }
}