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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.COMPILE
import com.toasttab.pulseman.AppStrings.COMPILING
import com.toasttab.pulseman.AppStrings.DELAY_MS
import com.toasttab.pulseman.AppStrings.GENERATE
import com.toasttab.pulseman.AppStrings.GENERATING
import com.toasttab.pulseman.AppStrings.RECOMPILE
import com.toasttab.pulseman.AppStrings.REPEAT
import com.toasttab.pulseman.AppStrings.SEND
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.view.ViewUtils.styledTextField
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import javax.swing.BoxLayout
import javax.swing.JPanel
import kotlinx.coroutines.CoroutineScope
import org.fife.ui.rtextarea.RTextScrollPane

/**
 * This view allows the user to
 * - Generate a code template of the selected class to serialize
 * - Edit and compile kotlin code to generate a pulsar message
 * - Send a pulsar message
 */
@Composable
fun sendProtobufUI(
    scope: CoroutineScope,
    generateState: ButtonState,
    onGenerateStateChange: (ButtonState) -> Unit,
    compileState: ButtonState,
    onCompileStateChange: (ButtonState) -> Unit,
    sendState: ButtonState,
    onSendStateChange: (ButtonState) -> Unit,
    sendButtonActiveText: String,
    sendButtonIsCancellable: Boolean,
    generateClassTemplate: () -> Unit,
    compileMessage: () -> Unit,
    sendPulsarMessage: () -> Unit,
    scrollPane: RTextScrollPane,
    isRepeatSelected: Boolean,
    onRepeatSelected: (Boolean) -> Unit,
    isRecompileSelected: Boolean,
    onRecompileSelected: (Boolean) -> Unit,
    delay: String,
    onDelayChanged: (String) -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            threadedButton(
                scope = scope,
                activeText = GENERATING,
                waitingText = GENERATE,
                buttonState = generateState,
                onButtonStateChange = onGenerateStateChange
            ) {
                generateClassTemplate()
            }

            threadedButton(
                scope = scope,
                activeText = COMPILING,
                waitingText = COMPILE,
                buttonState = compileState,
                onButtonStateChange = onCompileStateChange
            ) {
                compileMessage()
            }

            threadedButton(
                scope = scope,
                activeText = sendButtonActiveText,
                waitingText = SEND,
                isCancellable = sendButtonIsCancellable,
                buttonState = sendState,
                onButtonStateChange = onSendStateChange
            ) {
                sendPulsarMessage()
            }


            Text(text = REPEAT)
            Switch(
                checked = isRepeatSelected,
                onCheckedChange = onRepeatSelected,
                colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.colors.backgroundDark)
            )

            if (isRepeatSelected) {
                Text(text = RECOMPILE)
                Switch(
                    checked = isRecompileSelected,
                    onCheckedChange = onRecompileSelected,
                    colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.colors.backgroundDark)
                )

                styledTextField(
                    label = DELAY_MS,
                    field = delay,
                    modifier = Modifier.padding(2.dp).fillMaxWidth(0.3F),
                    background = AppTheme.colors.backgroundMedium,
                    border = AppTheme.colors.backgroundMedium,
                    onValueChange = onDelayChanged
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SwingPanel(
                background = Color.White,
                modifier = Modifier.fillMaxSize(),
                factory = {
                    JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        add(scrollPane)
                    }
                }
            )
        }
    }
}
