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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oleg.app.anki.Anki
import net.oleg.app.anki.AnkiResponse
import net.oleg.app.anki.ItemNames
import net.oleg.app.anki.ItemNamesList

@Composable
fun ColumnScope.ChooseItemNameRow(
    anki: Anki,
    itemNames: ItemNames,
    header: String,
    itemInitValue: String,
    updateSettings: (newItemValue: String) -> Unit
) {
    val currentScope = rememberCoroutineScope()

    var itemName by remember { mutableStateOf(itemInitValue) }
    var itemNamesResponse by remember { mutableStateOf<AnkiResponse<ItemNamesList>>(AnkiResponse.Init) }

    val message: String?
    val itemNamesList: ItemNamesList?
    when (val response = itemNamesResponse) {
        AnkiResponse.Init -> {
            message = null
            itemNamesList = null
        }
        AnkiResponse.Progress -> {
            message = "Progress..."
            itemNamesList = null
        }
        is AnkiResponse.Result -> {
            message = null
            itemNamesList = response.result
        }
        is AnkiResponse.Error -> {
            message = response.error
            itemNamesList = null
        }
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
            text = header
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
                onClick = {
                    currentScope.launch {
                        itemNamesResponse = AnkiResponse.Progress
                        itemNamesResponse = anki.getNames(itemNames.name)
                    }
                    expanded = true
                }
            ) {
                Text(text = itemName)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                itemNamesList?.forEach {
                    DropdownMenuItem(onClick = {
                        itemName = it
                        updateSettings(it)
                        expanded = false
                    }) {
                        Text(text = it)
                    }
                }
            }
        }

        message?.apply {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                text = this
            )
        }
    }
}