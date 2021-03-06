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

sealed class DictionaryResponse<out T> {
    object Init : DictionaryResponse<Nothing>()

    object Progress : DictionaryResponse<Nothing>()

    class Result<out T>(val value: T) : DictionaryResponse<T>()

    class Error(val error: String) : DictionaryResponse<Nothing>()
}