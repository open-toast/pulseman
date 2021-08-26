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

import androidx.compose.desktop.SwingPanel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.toasttab.pulseman.pulsar.Pulsar
import com.toasttab.pulseman.scripting.KotlinScripting
import com.toasttab.pulseman.state.SendMessage
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * This view allows the user to
 * - Generate a code template of the selected class to serialize
 * - Edit and compile kotlin code to generate a pulsar message
 * - Send a pulsar message
 */
@Composable
fun sendMessageUI(state: SendMessage) {
    Column {
        Row {
            threadedButton(state.scope, "Generating...", "Generate", state.generateState) {
                val currentlySelected = state.selectedClass.selected
                state.textArea.text = if (currentlySelected == null) {
                    state.setUserFeedback("No class selected")
                    ""
                } else {
                    state.setUserFeedback("Generated code template")
                    currentlySelected.generateClassTemplate()
                }
            }

            threadedButton(state.scope, "Compiling...", "Compile", state.compileState) {
                state.generatedBytes =
                    KotlinScripting.compileMessage(state.textArea.text, state.selectedClass, state.setUserFeedback)
            }

            threadedButton(state.scope, "Sending...", "Send", state.sendState) {
                val pulsar = Pulsar(state.pulsarSettings, state.setUserFeedback)
                try {
                    pulsar.sendMessage(state.generatedBytes)
                } catch (ex: Throwable) {
                    state.setUserFeedback("Failed to send message:$ex")
                } finally {
                    pulsar.close()
                }
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
                        add(state.sp)
                    }
                }
            )
        }
    }
}
