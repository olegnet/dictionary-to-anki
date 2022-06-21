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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oleg.app.anki.Anki
import net.oleg.app.dictionary.*
import net.oleg.app.settings.Settings

@Composable
fun App(
    anki: Anki,
    dictionary: Dictionary,
    settings: Settings,
) {
    val currentScope = rememberCoroutineScope()

    var translationOrder by remember { mutableStateOf(settings.translationOrder) }
    var deckName by remember { mutableStateOf(settings.deckName) }
    var modelName by remember { mutableStateOf(settings.modelName) }

    var ankiConnect by remember { mutableStateOf<Boolean?>(null) }
    var lookupResult by remember { mutableStateOf<RequestState<Lookup>>(RequestState.Nothing()) }
    var languagesResult by remember { mutableStateOf<RequestState<Languages>>(RequestState.Nothing()) }

    fun pingAnki() {
        currentScope.launch {
            ankiConnect = anki.requestPermission()
        }
    }

    SideEffect {
        pingAnki()

        currentScope.launch {
            languagesResult = RequestState.Progress()
            languagesResult = dictionary.getLanguages()
        }
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
                            lookupResult = dictionary.lookup(translationOrder, lookupString)
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

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(8.dp),
                        text = translationOrder
                    )

                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                    ) {
                        when (languagesResult) {
                            is RequestState.Nothing -> {}
                            is RequestState.Progress -> {
                                Text(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(8.dp),
                                    text = "Progress..."
                                )
                            }
                            is RequestState.Success -> {
                                languagesResult.value?.forEach {
                                    Text(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .padding(8.dp),
                                        text = it
                                    )
                                }
                            }
                            is RequestState.Failure -> {
                                ShowFailure(languagesResult.error)
                            }
                        }
                    }

                }
            }

            LookupResultColumn(lookupResult) { front, back ->
                currentScope.launch {
                    val result = anki.addNote(deckName, modelName, front, back)
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
