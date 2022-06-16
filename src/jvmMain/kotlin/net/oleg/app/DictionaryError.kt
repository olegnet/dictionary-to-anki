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
        private const val BAD_REQUEST = 400
        private const val INVALID_API_KEY = 401
        private const val API_KEY_BLOCKED = 402
        private const val DAILY_LIMIT_EXCEEDED = 403
        private const val TEXT_SIZE_EXCEEDED = 413
        private const val UNSUPPORTED_TRANSLATION_DIRECTION = 501

        // FIXME messages
        fun from(httpStatusCode: HttpStatusCode): DictionaryError =
            when (val code = httpStatusCode.value) {
                BAD_REQUEST ->
                    DictionaryError(code, "Bad request")
                INVALID_API_KEY ->
                    DictionaryError(code, "Invalid API key")
                API_KEY_BLOCKED ->
                    DictionaryError(code, "API key has been blocked")
                DAILY_LIMIT_EXCEEDED ->
                    DictionaryError(code, "Exceeded the daily limit of requests")
                TEXT_SIZE_EXCEEDED ->
                    DictionaryError(code, "Text size exceeded")
                UNSUPPORTED_TRANSLATION_DIRECTION ->
                    DictionaryError(code, "Translation direction is not supported")
                else ->
                    DictionaryError(code, "Unknown error $code: ${httpStatusCode.description}")
            }
    }
}