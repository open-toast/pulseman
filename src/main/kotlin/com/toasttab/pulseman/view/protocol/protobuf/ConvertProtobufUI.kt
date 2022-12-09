/*
 * Copyright (c) 2021 Toast Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toasttab.pulseman.view.protocol.protobuf

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.CONVERT
import com.toasttab.pulseman.AppStrings.CONVERTING
import com.toasttab.pulseman.AppStrings.ENTER_VALUE
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import kotlinx.coroutines.CoroutineScope

/**
 * This view allows the user to convert a serialized text format to a selected protobuf class
 */
@Composable
fun convertProtobufUI(
    scope: CoroutineScope,
    convertState: ButtonState,
    onConvertStateChange: (ButtonState) -> Unit,
    convert: () -> Unit,
    convertValue: String,
    onConvertValueChange: (String) -> Unit,
    convertTypeSelectorUI: @Composable () -> Unit,
    convertedMessage: String,
    convertValueScrollState: ScrollState,
    convertedScrollState: ScrollState
) {
    Column {
        Row {
            threadedButton(scope, CONVERTING, CONVERT, convertState, onConvertStateChange) {
                convert()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
                convertTypeSelectorUI()
            }
        }
        SelectionContainer {
            Box {
                val textFieldWeight = 0.5f
                TextField(
                    label = { Text(ENTER_VALUE) },
                    value = convertValue,
                    onValueChange = onConvertValueChange,
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(textFieldWeight)
                        .background(color = AppTheme.colors.backgroundLight)
                        .verticalScroll(convertValueScrollState)
                )
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(textFieldWeight).align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(convertValueScrollState)
                )
            }
        }
        Divider(color = AppTheme.colors.backgroundMedium, thickness = 2.dp)
        SelectionContainer {
            Box {
                Text(
                    text = convertedMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(AppTheme.colors.backgroundLight)
                        .verticalScroll(convertedScrollState)
                )
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd),
                    adapter = rememberScrollbarAdapter(convertedScrollState)
                )
            }
        }
    }
}
