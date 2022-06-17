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

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import net.oleg.app.anki.Anki
import net.oleg.app.dictionary.Dictionary
import net.oleg.app.ui.App
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

private val logger = LoggerFactory.default.newLogger("net.oleg.app", "MainKt")

fun main() = application {
    // FIXME disable logging
    val client = Network.client(enableLogging = true)

    val anki = Anki(client)
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
        App(anki, dictionary)
    }
}