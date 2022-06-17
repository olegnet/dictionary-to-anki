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

@file:OptIn(ExperimentalComposeUiApi::class)

package net.oleg.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oleg.app.anki.Anki
import net.oleg.app.dictionary.Dictionary
import net.oleg.app.dictionary.DictionaryError
import net.oleg.app.dictionary.Lookup
import net.oleg.app.dictionary.RequestState

@Composable
fun App(
    anki: Anki,
    dictionary: Dictionary,
) {
    val currentScope = rememberCoroutineScope()

    var ankiConnect by remember { mutableStateOf<Boolean?>(null) }
    var languageOrder by remember { mutableStateOf("en-ru") }
    var lookupResult by remember { mutableStateOf<RequestState<Lookup>>(RequestState.Nothing()) }

    fun pingAnki() {
        currentScope.launch {
            ankiConnect = anki.requestPermission()
        }
    }

    SideEffect {
        pingAnki()
    }

    MaterialTheme(
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
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top,
            ) {

                LookupRow { lookupString ->
                    currentScope.launch {
                        if (lookupString.trim().isEmpty()) {
                            lookupResult = RequestState.Nothing()   // FIXME add message
                        } else {
                            lookupResult = RequestState.Progress()  // FIXME move flow inside lookup fun
                            lookupResult = dictionary.lookup(languageOrder, lookupString)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(8.dp),
                        text = when (ankiConnect) {
                            true -> "Anki is available"
                            false -> "Anki is not available"
                            else -> "Connecting to Anki..."
                        }
                    )
                    IconButton(
                        modifier = Modifier
                            .wrapContentSize(),
                        onClick = {
                            ankiConnect = null
                            pingAnki()
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh Anki status")
                    }
                }

                // FIXME choose languages here
            }

            LookupResultColumn(lookupResult) { front, back ->
                currentScope.launch {
                    val result = anki.addNote(front, back)
                    // FIXME show progress and the result
                }
            }
        }
    }
}

@Composable
fun ShowFailure(error: DictionaryError?) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        text = error?.message ?: "Unknown error"
    )
}

@Composable
fun ShowNoResult() {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        text = "No result"
    )
}