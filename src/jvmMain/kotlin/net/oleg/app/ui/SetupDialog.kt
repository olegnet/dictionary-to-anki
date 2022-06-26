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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import net.oleg.app.settings.Keys

@Composable
fun SetupDialog(
    onCloseRequest: () -> Unit,
) {
    Dialog(
        state = DialogState(
            position = WindowPosition(alignment = Alignment.Center),
            size = DpSize(600.dp, 600.dp),
        ),
        title = "Dictionary to Anki: Setup",
        onCloseRequest = onCloseRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Get a free key for dictionary API " +
                    "from https://yandex.com/dev/dictionary/ " +
                    "and put it into ${Keys.defaultPath} as '${Keys.DICTIONARY_KEY}' property\n" +
                    "Please restart application after saving the file")

            val buttonModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp, vertical = 16.dp)

            Button(
                modifier = buttonModifier,
                onClick = { openBrowser(Keys.DICTIONARY_API_URL) },
            ) {
                Text(text = "Open browser")
            }

            Button(
                modifier = buttonModifier,
                onClick = { setClipboard(Keys.DICTIONARY_API_URL) },
            ) {
                Text("Add url to clipboard")
            }

            Button(modifier = buttonModifier,
                onClick = {
                    Keys.createPropertyFile()
                    openEditor(Keys.defaultPath)
                }
            ) {
                Text("Create file and open editor")
            }

            Button(
                modifier = buttonModifier,
                onClick = { setClipboard(Keys.defaultPath) },
            ) {
                Text("Add file path to clipboard")
            }
        }
    }
}