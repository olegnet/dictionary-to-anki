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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Translations(
    @SerialName("text") val text: String,
    @SerialName("pos") val partOfSpeech: String,
    // val gen: String? = null,
    // val fr: Int? = null,
    //  syn     Array of synonyms
    //  mean    Array of meanings
    //  ex	    Array of examples
)