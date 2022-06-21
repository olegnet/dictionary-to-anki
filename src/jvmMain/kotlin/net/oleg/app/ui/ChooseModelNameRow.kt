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
import net.oleg.app.anki.ModelNames
import net.oleg.app.anki.Response
import net.oleg.app.settings.Settings

@Composable
fun ColumnScope.ChooseModelNameRow(
    anki: Anki,
    settings: Settings,
) {
    val currentScope = rememberCoroutineScope()

    var modelName by remember { mutableStateOf(settings.modelName) }
    var modelNamesResponse by remember { mutableStateOf<Response<ModelNames>>(Response()) }

    fun getModelNames() {
        currentScope.launch {
            modelNamesResponse = anki.getModelNames()
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
            text = "Model name"
        )

        modelNamesResponse.error?.apply {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                text = this
            )
        }

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
                    getModelNames()
                    expanded = true
                }
            ) {
                Text(text = modelName)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                modelNamesResponse.result?.forEach {
                    DropdownMenuItem(onClick = {
                        modelName = it
                        settings.modelName = it
                        expanded = false
                    }) {
                        Text(text = it)
                    }
                }
            }
        }
    }
}