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

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import net.oleg.app.dictionary.DictionaryEntry
import org.kodein.log.LoggerFactory
import org.kodein.log.newLogger

private val logger = LoggerFactory.default.newLogger("net.oleg.app.ui", "ShowDictionaryEntriesKt")

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ColumnScope.ShowDictionaryEntries(
    entries: List<DictionaryEntry>,
    addNote: (String, String) -> Unit,
) {
    logger.debug { "entries: $entries" }

    entries.forEach { dict ->
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
            Text(
                modifier = Modifier
                    .wrapContentSize(),
                color = Color.LightGray,
                text = dict.partOfSpeech
            )
        }

        dict.translations.forEach { translation ->
            val keyPressedState = remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }
            val backgroundColor =
                if (interactionSource.collectIsHoveredAsState().value) {
                    MaterialTheme.colors.secondary
                } else {
                    if (interactionSource.collectIsFocusedAsState().value) {
                        if (keyPressedState.value)
                            lerp(MaterialTheme.colors.secondary, Color(64, 64, 64), 0.3f)
                        else
                            MaterialTheme.colors.secondary
                    } else {
                        MaterialTheme.colors.primary
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
}