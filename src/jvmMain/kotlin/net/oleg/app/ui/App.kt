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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import kotlinx.coroutines.launch
import net.oleg.app.anki.Anki
import net.oleg.app.anki.ItemNames
import net.oleg.app.dictionary.Dictionary
import net.oleg.app.dictionary.DictionaryResponse
import net.oleg.app.dictionary.Lookup
import net.oleg.app.settings.Settings

@Composable
fun App(
    anki: Anki,
    dictionary: Dictionary,
    settings: Settings,
) {
    val currentScope = rememberCoroutineScope()
    var lookupResult by remember { mutableStateOf<DictionaryResponse<Lookup>>(DictionaryResponse.Init) }

    MaterialTheme(
        // FIXME colors
        colors = MaterialTheme.colors.copy(
            primary = Color(201, 208, 212),
            secondary = Color(119, 163, 189)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(0.dp)
                    .weight(1f)
                    .background(color = Color.White)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
            ) {
                LookupRow { lookupString ->
                    currentScope.launch {
                        if (lookupString.trim().isEmpty()) {
                            lookupResult = DictionaryResponse.Init
                        } else {
                            lookupResult = DictionaryResponse.Progress
                            lookupResult = dictionary.lookup(settings.translationOrder, lookupString)
                        }
                    }
                }

                ChooseLanguageRow(dictionary, settings)

                AnkiConnectPingRow(anki)

                ChooseItemNameRow(anki, ItemNames.deckNames, "Deck name", settings.deckName) {
                    settings.deckName = it
                }

                ChooseItemNameRow(anki, ItemNames.modelNames, "Model name", settings.modelName) {
                    settings.modelName = it
                }

                OpenSetupDialogRow()
            }

            LookupResultColumn(anki, settings, lookupResult)
        }
    }
}

@Composable
private fun OpenSetupDialogRow() {
    var isDialogOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(8.dp),
            text = "Setup"
        )
        IconButton(onClick = { isDialogOpen = true }) {
            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Open setup dialog")
        }
    }

    if (isDialogOpen) {
        SetupDialog { isDialogOpen = false }
    }
}