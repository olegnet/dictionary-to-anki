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
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    viewModel: ViewModel,
) {
    val logger = LoggerFactory.default.newLogger("net.oleg.app", "MainKt")
    val currentScope = rememberCoroutineScope()

    var search by remember { mutableStateOf("") }
    var languageOrder by remember { mutableStateOf("en-ru") }
//    var languages by remember { mutableStateOf<Languages>(listOf()) }
    var lookup by remember { mutableStateOf(Lookup(listOf())) }

    MaterialTheme {
        Row(
            modifier = Modifier.fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 32.dp, alignment = Alignment.CenterHorizontally),
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 32.dp, alignment = Alignment.Start),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .align(Alignment.Top),
                    value = search,
                    onValueChange = { search = it },
                    label = @Composable { Text("Search") },
                    singleLine = true,
                )

                Button(
                    modifier = Modifier
                        .align(Alignment.Top),
                    onClick = {
                        currentScope.launch {
//                        languages = viewModel.getLanguages()
//                        logger.debug { "languages: $languages" }

                            lookup = viewModel.lookup(languageOrder, search)
                            logger.debug { "lookup: $lookup" }
                        }
                    }
                ) {
                    Text("Submit")
                }
            }
/*            Text(
                modifier = Modifier.fillMaxSize()
                    .padding(8.dp),
                text = languages.toString()
            )*/

            lookup.def.forEach { dict ->
                Text(
                    modifier = Modifier.fillMaxSize()
                        .padding(8.dp),
                    text = dict.toString()
                )
            }
        }
    }
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
    val viewModel = ViewModel(client)

    Window(
        onCloseRequest = {
            client.close()
            exitApplication()
        },
        resizable = true,
    ) {
        App(viewModel)
    }
}
