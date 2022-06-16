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

package net.oleg.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App(
    dictionary: Dictionary,
) {
    val logger = LoggerFactory.default.newLogger("net.oleg.app", "MainKt")
    val currentScope = rememberCoroutineScope()

    var languageOrder by remember { mutableStateOf("en-ru") }
    var lookupResult by remember { mutableStateOf<RequestState<Lookup>>(RequestState.Nothing()) }

    MaterialTheme {
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
                verticalArrangement = Arrangement.Top
            ) {

                LookupRow { lookupString ->
                    currentScope.launch {
                        if (lookupString.trim().isEmpty()) {
                            lookupResult = RequestState.Nothing()   // FIXME add message
                        } else {
                            lookupResult = RequestState.Progress()  // FIXME move flow inside lookup fun
                            lookupResult = dictionary.lookup(languageOrder, lookupString)
                            logger.debug { "lookupResult: $lookupResult" }
                        }
                    }
                }
            }

            LookupResultColumn(lookupResult)
        }
    }
}

@Composable
private fun ColumnScope.LookupRow(
    lookup: (lookupString: String) -> Unit
) {
    var lookupString by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .wrapContentSize()
                .onPreviewKeyEvent {
                    if (it.key == Key.Enter && it.type == KeyEventType.KeyUp) {
                        lookup(lookupString)
                    }
                    false
                },
            value = lookupString,
            onValueChange = { lookupString = it },
            label = @Composable { Text("Search") },
            singleLine = true,
        )
        IconButton(
            modifier = Modifier
                .padding(10.dp),
            onClick = { lookup(lookupString) }
        ) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
        }
    }
}

@Composable
private fun RowScope.LookupResultColumn(
    lookupResult: RequestState<Lookup>
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(0.dp)
            .weight(1f)
            .background(color = Color.White)
            .padding(8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        when (lookupResult) {
            is RequestState.Nothing -> {}
            is RequestState.Progress -> {
                Text(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    text = "Progress..."
                )
            }
            is RequestState.Success -> {
                val def = lookupResult.value?.def
                if (def?.size == 0) {
                    ShowNoResult()
                } else {
                    def?.forEach { dict ->
                        Text(
                            modifier = Modifier.fillMaxSize()
                                .padding(8.dp),
                            text = dict.toString()
                        )
                    }
                }
            }
            is RequestState.Failure ->
                ShowFailure(lookupResult.error)
        }
    }
}

@Composable
private fun ShowFailure(error: DictionaryError?) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        text = error?.message ?: "Unknown error"
    )
}

@Composable
private fun ShowNoResult() {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        text = "No result"
    )
}

fun main() = application {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            developmentMode = true
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }
    val dictionary = Dictionary(client)

    Window(
        title = "Dictionary",
        onCloseRequest = {
            client.close()
            exitApplication()
        },
        resizable = true,
    ) {
        App(dictionary)
    }
}
