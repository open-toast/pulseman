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

package com.toasttab.pulseman.state.protocol.protobuf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.AppStrings.FAILED_TO_SEND_MESSAGE
import com.toasttab.pulseman.AppStrings.GENERATED_CODE_TEMPLATE
import com.toasttab.pulseman.AppStrings.NO_CLASS_SELECTED
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.pulsar.Pulsar
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.scripting.KotlinScripting
import com.toasttab.pulseman.state.PulsarSettings
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import com.toasttab.pulseman.view.protocol.protobuf.sendProtobufUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

class SendProtobufMessage(
    val setUserFeedback: (String) -> Unit,
    val selectedClass: SingleSelection<PulsarMessageClassInfo>,
    val pulsarSettings: PulsarSettings,
    onChange: () -> Unit,
    initialSettings: TabValuesV3? = null
) {
    private val generateState = mutableStateOf(ButtonState.WAITING)
    private val sendState = mutableStateOf(ButtonState.WAITING)
    private val compileState = mutableStateOf(ButtonState.WAITING)

    private var generatedBytes: ByteArray? = null

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val textArea =
        RSyntaxTextArea.textArea(
            initialSettings?.protobufSettings?.code ?: "",
            SyntaxConstants.SYNTAX_STYLE_JAVA,
            onChange
        )

    private fun generateClassTemplate() {
        textArea.text = selectedClass.selected?.let {
            setUserFeedback(GENERATED_CODE_TEMPLATE)
            it.generateClassTemplate()
        } ?: run {
            setUserFeedback(NO_CLASS_SELECTED)
            ""
        }
    }

    fun close() {
        scope.cancel(CANCEL_SCOPE_LOG)
    }

    private fun compileMessage() {
        generatedBytes =
            KotlinScripting.compileMessage(textArea.text, selectedClass, setUserFeedback)
    }

    private fun sendPulsarMessage() {
        val pulsar = Pulsar(pulsarSettings, setUserFeedback)
        try {
            pulsar.sendMessage(generatedBytes)
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_SEND_MESSAGE:$ex")
        } finally {
            pulsar.close()
        }
    }

    private val sp = RTextScrollPane(textArea)

    fun currentCode(): String = textArea.text

    fun getUI(): @Composable () -> Unit {
        return {
            sendProtobufUI(
                scope = scope,
                generateState = generateState.value,
                onGenerateStateChange = generateState::onStateChange,
                compileState = compileState.value,
                onCompileStateChange = compileState::onStateChange,
                sendState = sendState.value,
                onSendStateChange = sendState::onStateChange,
                generateClassTemplate = ::generateClassTemplate,
                compileMessage = ::compileMessage,
                sendPulsarMessage = ::sendPulsarMessage,
                scrollPane = sp
            )
        }
    }

    companion object {
        private const val CANCEL_SCOPE_LOG = "Shutting down SendProtobufMessage"
    }
}
