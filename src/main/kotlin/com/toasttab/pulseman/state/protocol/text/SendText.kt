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

package com.toasttab.pulseman.state.protocol.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.AppStrings.FAILED_TO_SEND_MESSAGE
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.TabValuesV2
import com.toasttab.pulseman.pulsar.Pulsar
import com.toasttab.pulseman.state.PulsarSettings
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import com.toasttab.pulseman.view.protocol.protobuf.sendTextUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

@Suppress("UNUSED_PARAMETER")
class SendText(
    private val serializationTypeSelector: SerializationTypeSelector,
    val appState: AppState,
    val setUserFeedback: (String) -> Unit,
    val pulsarSettings: PulsarSettings,
    onChange: () -> Unit,
    initialSettings: TabValuesV2? = null,
) {
    private val sendState = mutableStateOf(ButtonState.WAITING)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val textArea =
        RSyntaxTextArea.textArea(
            initialSettings?.textSettings?.text ?: "",
            SyntaxConstants.SYNTAX_STYLE_JAVA
        ) { } // TODO removing call to onChange as we lose focus on recomposition with RSyntaxTextArea. May be hard to fix

    fun close() {
        scope.cancel(CANCEL_SCOPE_LOG)
    }

    private fun sendPulsarMessage() {
        val pulsar = Pulsar(pulsarSettings, setUserFeedback)
        try {
            pulsar.sendMessage(serializationTypeSelector.selectedSendEncoding.selected?.serialize(textArea.text))
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_SEND_MESSAGE:$ex")
        } finally {
            pulsar.close()
        }
    }

    private val sp = RTextScrollPane(textArea)

    fun currentText(): String = textArea.text

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            sendTextUI(
                scope = scope,
                sendState = sendState.value,
                onSendStateChange = sendState::onStateChange,
                sendPulsarMessage = ::sendPulsarMessage,
                scrollPane = sp
            )
        }
    }

    companion object {
        private const val CANCEL_SCOPE_LOG = "Shutting down SendText"
    }
}
