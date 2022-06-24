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

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.oleg.app.anki.Anki
import net.oleg.app.anki.Response
import net.oleg.app.dictionary.DictionaryEntry
import net.oleg.app.settings.Settings
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

private val logger = LoggerFactory.default.newLogger("net.oleg.app.ui", "ShowDictionaryEntriesKt")

@Composable
fun ColumnScope.ShowDictionaryEntries(
    anki: Anki,
    settings: Settings,
    entries: List<DictionaryEntry>,
) {
    val currentScope = rememberCoroutineScope()
    var addNoteResult by remember { mutableStateOf<Response<Long?>>(Response()) }

    val message: String
    val messageColor: Color

    if (addNoteResult.result != null) {
        message = "Added new note with id ${addNoteResult.result}"
        messageColor = MaterialTheme.colors.secondaryVariant
    } else {
        val error = addNoteResult.error
        if (error != null) {
            message = "Anki: $error"
            messageColor = MaterialTheme.colors.error
        } else {
            message = ""
            messageColor = Color.Transparent
        }
    }

    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp),
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
            text = message,
            color = messageColor
        )
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
    ) {
        entries.forEach { dict ->
            ShowDictionaryEntry(dict) { front, back ->
                currentScope.launch {
                    addNoteResult = anki.addNote(settings.deckName, settings.modelName, front, back)
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ShowDictionaryEntryHeader(
    dict: DictionaryEntry,
) {
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
        if (!dict.partOfSpeech.isNullOrEmpty()) {
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                color = Color.LightGray,
                text = dict.partOfSpeech
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.ShowDictionaryEntry(
    dict: DictionaryEntry,
    addNote: (String, String) -> Unit,
) {
    val hoveredStateColor = MaterialTheme.colors.secondary
    val focusedPressedStateColor = lerp(MaterialTheme.colors.secondary, Color(64, 64, 64), 0.3f)
    val focusedUnpressedStateColor = MaterialTheme.colors.secondary
    val unfocusedStateColor = MaterialTheme.colors.primary

    ShowDictionaryEntryHeader(dict)

    dict.translations.forEach { translation ->
        val interactionSource = remember { MutableInteractionSource() }
        val keyPressedState = remember { mutableStateOf(false) }

        // FIXME restore lost focused state after hover
        val backgroundColor =
            if (interactionSource.collectIsHoveredAsState().value) {
                hoveredStateColor
            } else {
                if (interactionSource.collectIsFocusedAsState().value) {
                    if (keyPressedState.value) focusedPressedStateColor else focusedUnpressedStateColor
                } else {
                    unfocusedStateColor
                }
            }

        Box(
            modifier = Modifier
                .padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .fillMaxWidth()
                .background(backgroundColor)
                .onPreviewKeyEvent {
                    if (it.key == Key.Enter || it.key == Key.Spacebar) {
                        when (it.type) {
                            KeyEventType.KeyDown -> {
                                keyPressedState.value = true
                            }
                            KeyEventType.KeyUp -> {
                                keyPressedState.value = false
                                logger.debug { "onPreviewKeyEvent: ${dict.text} -> ${translation.text}" }
                                addNote(dict.text, translation.text)
                            }
                        }
                    }
                    false
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = {
                        logger.debug { "onClick: ${dict.text} -> ${translation.text}" }
                        addNote(dict.text, translation.text)
                    },
                ),
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