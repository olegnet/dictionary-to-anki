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

package net.oleg.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.oleg.app.anki.Anki
import net.oleg.app.dictionary.DictionaryResponse
import net.oleg.app.dictionary.Lookup
import net.oleg.app.settings.Settings

@Composable
fun RowScope.LookupResultColumn(
    anki: Anki,
    settings: Settings,
    lookupResult: DictionaryResponse<Lookup>,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(0.dp)
            .weight(1f)
            .background(color = Color.White)
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        when (lookupResult) {
            is DictionaryResponse.Init -> {}
            is DictionaryResponse.Progress -> {
                ShowMessage("Progress...")
            }
            is DictionaryResponse.Result -> {
                val entries = lookupResult.value.dictionaryEntries
                if (entries.isNotEmpty()) {
                    ShowDictionaryEntries(anki, settings, entries)
                } else {
                    ShowMessage("No result")
                }
            }
            is DictionaryResponse.Error -> {
                ShowMessage(lookupResult.error)
            }
        }
    }
}

@Composable
private fun ShowMessage(message: String) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        text = message
    )
}