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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oleg.app.anki.Anki
import net.oleg.app.anki.Response

@Composable
fun ColumnScope.AnkiConnectPingRow(
    anki: Anki,
) {
    val currentScope = rememberCoroutineScope()

    var ankiConnectResponse by remember { mutableStateOf<Response<Boolean>?>(null) }

    fun pingAnki() {
        currentScope.launch {
            ankiConnectResponse = anki.requestPermission()
        }
    }

    LaunchedEffect(this) {
        pingAnki()
    }

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ankiConnectResponse?.result.apply {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                text = when (this) {
                    true -> "Anki is available"
                    false -> "Anki is not available"    // Never happen, showing 'error' instead
                    else -> "Connecting to Anki..."
                }
            )
        }

        ankiConnectResponse?.error?.apply {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                text = this
            )
        }

        IconButton(onClick = {
            ankiConnectResponse = null
            pingAnki()
        }) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh Anki status")
        }
    }
}