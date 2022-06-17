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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.LookupRow(
    lookup: (lookupString: String) -> Unit,
) {
    var lookupString by remember { mutableStateOf("") }

    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        androidx.compose.material.OutlinedTextField(
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