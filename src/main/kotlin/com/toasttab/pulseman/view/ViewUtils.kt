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

package com.toasttab.pulseman.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ButtonState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Reusable UI components
 */
object ViewUtils {
    @Composable
    fun styledTextField(
        label: String? = null,
        placeholder: String? = null,
        field: String = "",
        modifier: Modifier,
        background: Color = AppTheme.colors.backgroundLight,
        border: Color = AppTheme.colors.backgroundMedium,
        onValueChange: (String) -> Unit
    ) = TextField(
        label = { label?.let { Text(it) } },
        placeholder = { placeholder?.let { Text(it) } },
        value = field,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier
            .background(color = background)
            .border(2.dp, border)
    )

    @Composable
    fun threadedButton(
        scope: CoroutineScope,
        activeText: String,
        waitingText: String,
        isCancellable: Boolean = false,
        buttonState: ButtonState,
        onButtonStateChange: (ButtonState) -> Unit,
        action: suspend () -> Unit
    ) {
        Button(
            modifier = Modifier.padding(4.dp),
            enabled = buttonState == ButtonState.WAITING || isCancellable,
            onClick = {
                when (buttonState) {
                    ButtonState.WAITING -> {
                        onButtonStateChange(ButtonState.ACTIVE)
                        scope.launch {
                            action()
                            onButtonStateChange(ButtonState.WAITING)
                        }
                    }

                    ButtonState.ACTIVE -> {
                        if (isCancellable) {
                            scope.launch {
                                action()
                            }
                        }
                    }
                }
            }
        ) {
            when (buttonState) {
                ButtonState.ACTIVE -> Text(activeText)
                ButtonState.WAITING -> Text(waitingText)
            }
        }
    }
}
