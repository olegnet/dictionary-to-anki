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

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
@Preview
fun App(
    dictionary: Dictionary,
) {
    val logger = LoggerFactory.default.newLogger("net.oleg.app", "MainKt")
    val currentScope = rememberCoroutineScope()

    var search by remember { mutableStateOf("") }
    var languageOrder by remember { mutableStateOf("en-ru") }

    var lookup by remember { mutableStateOf<RequestState<Lookup>>(RequestState.Nothing()) }

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

                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .wrapContentSize(),
                        value = search,
                        onValueChange = { search = it },
                        label = @Composable { Text("Search") },
                        singleLine = true,
                    )

                    IconButton(
                        modifier = Modifier
                            .padding(10.dp),
                        onClick = {
                            currentScope.launch {
                                lookup = RequestState.Progress()    // FIXME
                                lookup = dictionary.lookup(languageOrder, search)
                                logger.debug { "lookup: $lookup" }
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(0.dp)
                    .weight(1f)
                    .background(color = Color.White)
                    .padding(8.dp),
                verticalArrangement = Arrangement.Top
            ) {
                when (lookup) {
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
                        val def = lookup.value?.def
                        def?.forEach { dict ->
                            Text(
                                modifier = Modifier.fillMaxSize()
                                    .padding(8.dp),
                                text = dict.toString()
                            )
                        }
                        if (def?.size == 0) {
                            Text(
                                modifier = Modifier.fillMaxSize()
                                    .padding(8.dp),
                                text = "No result"
                            )
                        }
                    }
                    is RequestState.Failure ->
                        onFailure(lookup.error)
                }
            }
        }
    }
}

@Composable
private fun onFailure(error: DictionaryError?) =
    Text(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        text = error?.message ?: "Unknown error"
    )

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
