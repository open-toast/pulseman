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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import com.toasttab.pulseman.AppStrings.SEND
import com.toasttab.pulseman.AppStrings.SENDING
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import kotlinx.coroutines.CoroutineScope
import org.fife.ui.rtextarea.RTextScrollPane
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * This view allows the user to
 * - Edit the text to send in a message
 * - Select the serialization format of that message
 * - Send a pulsar message
 */
@Composable
fun sendTextUI(
    scope: CoroutineScope,
    sendState: ButtonState,
    onSendStateChange: (ButtonState) -> Unit,
    sendPulsarMessage: () -> Unit,
    scrollPane: RTextScrollPane
) {
    Column {
        Row {
            threadedButton(scope, SENDING, SEND, sendState, onSendStateChange) {
                sendPulsarMessage()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SwingPanel(
                background = Color.Transparent,
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
