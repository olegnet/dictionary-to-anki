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

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oleg.app.dictionary.Dictionary
import net.oleg.app.dictionary.DictionaryResponse
import net.oleg.app.dictionary.Languages
import net.oleg.app.settings.Settings

@Composable
fun ColumnScope.ChooseLanguageRow(
    dictionary: Dictionary,
    settings: Settings,
) {
    val currentScope = rememberCoroutineScope()

    var translationOrder by remember { mutableStateOf(settings.translationOrder) }
    var languagesResult by remember { mutableStateOf<DictionaryResponse<Languages>>(DictionaryResponse.Init) }

    fun loadLanguages() {
        currentScope.launch {
            languagesResult = DictionaryResponse.Progress
            languagesResult = dictionary.getLanguages()
        }
    }

    LaunchedEffect(this) {
        loadLanguages()
    }

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
            text = "Translation"
        )

        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart)
        ) {
            OutlinedButton(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                onClick = { expanded = true }
            ) {
                Text(text = translationOrder)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                (languagesResult as? DictionaryResponse.Result)?.value?.forEach {
                    DropdownMenuItem(onClick = {
                        translationOrder = it
                        settings.translationOrder = it
                        expanded = false
                    }) {
                        Text(text = it)
                    }
                }
            }
        }

        IconButton(onClick = { loadLanguages() }) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Load available languages")
        }

        if (languagesResult is DictionaryResponse.Progress) {
            Text(text = "Loading...")
        } else if (languagesResult is DictionaryResponse.Error) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                text = (languagesResult as DictionaryResponse.Error).error
            )
        }
    }
}