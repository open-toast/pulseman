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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
        label: String,
        field: String,
        modifier: Modifier,
        onValueChange: (String) -> Unit
    ) = TextField(
        label = { Text(label) },
        value = field,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier
            .background(color = AppTheme.colors.backgroundLight)
            .border(2.dp, AppTheme.colors.backgroundMedium)
    )

    @Composable
    fun threadedButton(
        scope: CoroutineScope,
        activeText: String,
        waitingText: String,
        state: MutableState<ButtonState>,
        action: suspend () -> Unit
    ) {
        val buttonState = remember(state) { state }

        Button(
            modifier = Modifier.padding(4.dp),
            enabled = buttonState.value == ButtonState.WAITING,
            onClick = {
                if (buttonState.value == ButtonState.WAITING) {
                    buttonState.value = ButtonState.ACTIVE
                    scope.launch {
                        action()
                        buttonState.value = ButtonState.WAITING
                    }
                }
            }
        ) {
            when (buttonState.value) {
                ButtonState.ACTIVE -> Text(activeText)
                ButtonState.WAITING -> Text(waitingText)
            }
        }
    }
}
