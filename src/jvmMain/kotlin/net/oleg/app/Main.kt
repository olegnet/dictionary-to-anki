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

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

val logger = LoggerFactory.default.newLogger("net.oleg.app", "MainKt")

@Composable
fun App(
    dictionary: Dictionary,
) {
    val currentScope = rememberCoroutineScope()

    var languageOrder by remember { mutableStateOf("en-ru") }
    var lookupResult by remember { mutableStateOf<RequestState<Lookup>>(RequestState.Nothing()) }

    MaterialTheme(
/*  FIXME
        colors = MaterialTheme.colors.copy(
            primary = ,
            secondary =
        ),
        typography = Typography(
        )
*/
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

                // FIXME choose languages here
            }

            LookupResultColumn(lookupResult)
        }
    }
}

@Composable
private fun ColumnScope.LookupRow(
    lookup: (lookupString: String) -> Unit,
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
    lookupResult: RequestState<Lookup>,
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
                val def = lookupResult.value?.dictionaryEntries
                if (def.isNullOrEmpty()) {
                    ShowNoResult()
                } else {
                    ShowDictionaryEntries(def)
                }
            }
            is RequestState.Failure -> {
                ShowFailure(lookupResult.error)
            }
        }
    }
}

@Composable
private fun ColumnScope.ShowDictionaryEntries(
    entries: List<DictionaryEntry>,
) {
    logger.debug { "entries: $entries" }

    entries.forEach { dict ->
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 4.dp),
                text = dict.text
            )
            if (!dict.transcription.isNullOrEmpty()) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 4.dp),
                    color = Color.LightGray,
                    text = "[${dict.transcription}]"
                )
            }
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                color = Color.LightGray,
                text = dict.partOfSpeech
            )
        }

        dict.translations.forEach { translation ->
            val keyPressedState = remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            val backgroundColor = if (interactionSource.collectIsFocusedAsState().value) {
                if (keyPressedState.value)
                    lerp(MaterialTheme.colors.secondary, Color(64, 64, 64), 0.3f)
                else
                    MaterialTheme.colors.secondary
            } else {
                MaterialTheme.colors.primary
            }

            Box(
                modifier = Modifier
                    .padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .onPointerEvent(eventType = PointerEventType.Press, pass = PointerEventPass.Main) {
                        logger.debug { "onPointerEvent: ${dict.text} -> ${translation.text}" }
                    }
                    .onPreviewKeyEvent {
                        if (it.key == Key.Enter || it.key == Key.Spacebar) {
                            when (it.type) {
                                KeyEventType.KeyDown -> {
                                    keyPressedState.value = true
                                }
                                KeyEventType.KeyUp -> {
                                    keyPressedState.value = false
                                    logger.debug { "onPreviewKeyEvent: ${dict.text} -> ${translation.text}" }
                                }
                            }
                        }
                        false
                    }
                    .focusable(interactionSource = interactionSource),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp),
                    text = translation.text,
                )
            }
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
        state = WindowState(size = DpSize(1000.dp, 800.dp)),
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
