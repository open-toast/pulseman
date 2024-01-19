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
import com.toasttab.pulseman.AppStrings.CANCELLED_REPEAT_MESSAGES
import com.toasttab.pulseman.AppStrings.COMPILING_MESSAGE
import com.toasttab.pulseman.AppStrings.EXITING_REPEAT_LOOP
import com.toasttab.pulseman.AppStrings.FAILED_TO_SEND_MESSAGE
import com.toasttab.pulseman.AppStrings.FAILS
import com.toasttab.pulseman.AppStrings.GENERATED_CODE_TEMPLATE
import com.toasttab.pulseman.AppStrings.MS
import com.toasttab.pulseman.AppStrings.NO_CLASS_SELECTED
import com.toasttab.pulseman.AppStrings.RETRYING
import com.toasttab.pulseman.AppStrings.RUNS
import com.toasttab.pulseman.AppStrings.SENDING
import com.toasttab.pulseman.AppStrings.SENDING_PROTOBUF
import com.toasttab.pulseman.AppStrings.SEND_TIME
import com.toasttab.pulseman.AppStrings.SLEPT
import com.toasttab.pulseman.AppStrings.STOP
import com.toasttab.pulseman.AppStrings.WAIT_SEND_TO_FINISH
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.CompileResult
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.pulsar.Pulsar
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.scripting.KotlinScripting
import com.toasttab.pulseman.state.PulsarSettings
import com.toasttab.pulseman.state.onChangeDigits
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import com.toasttab.pulseman.view.protocol.protobuf.sendProtobufUI
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

class SendProtobufMessage(
    val setUserFeedback: (String) -> Unit,
    val selectedClass: SingleSelection<PulsarMessageClassInfo>,
    val pulsarSettings: PulsarSettings,
    onChange: () -> Unit,
    initialSettings: TabValuesV3? = null
) {
    private var pulsar: Pulsar? = null
    private val generateState = mutableStateOf(ButtonState.WAITING)
    private val sendState = mutableStateOf(ButtonState.WAITING)
    private val compileState = mutableStateOf(ButtonState.WAITING)
    private val sendButtonActiveText = mutableStateOf(SENDING)
    private val isRepeatSelected = mutableStateOf(false)
    private val isRecompileSelected = mutableStateOf(false)
    private val delayString = mutableStateOf("0")
    private var sendRepeatMessages = false
    private var repeatingMessageJob: CompletableJob? = null

    private var compileResult: CompileResult? = null

    private val delay: Int
        get() = delayString.value.toIntOrNull() ?: 0

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
        setUserFeedback(COMPILING_MESSAGE)
        compileResult = KotlinScripting.compileMessage(textArea.text, selectedClass, setUserFeedback)
    }

    private fun recompileMessage() {
        compileResult?.let {
            setUserFeedback(COMPILING_MESSAGE)
            compileResult = KotlinScripting.recompile(it, setUserFeedback)
        }
    }

    private fun sendPulsarMessage() {
        repeatingMessageJob?.let {
            cancelRepeatJob()
            return
        }

        if (isRepeatSelected.value) {
            sendRepeatingPulsarMessages()
        } else {
            sendSinglePulsarMessage()
        }
    }

    private fun sendSinglePulsarMessage() {
        pulsar = Pulsar(pulsarSettings, setUserFeedback)
        try {
            pulsar?.sendMessage(compileResult?.bytes)
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_SEND_MESSAGE:$ex")
        } finally {
            pulsar?.close()
        }
    }

    private fun sendRepeatingPulsarMessages() {
        val job = createRepeatJob()
        pulsar = Pulsar(pulsarSettings, setUserFeedback)
        var runs = 0
        var fails = 0
        var sleepTime = 0L
        var duration = 0L

        try {
            while (sendRepeatMessages) {
                val startTime = System.currentTimeMillis()
                if (runs == 0) {
                    compileMessage()
                } else if (isRecompileSelected.value) {
                    recompileMessage()
                }

                setUserFeedback("$SENDING_PROTOBUF $RUNS:${runs++} $SLEPT:$sleepTime$MS $SEND_TIME:$duration$MS")
                if (pulsar?.sendMessage(compileResult?.bytes) != true) {
                    if (runs == 1 || fails++ == MAX_FAILS) {
                        setUserFeedback("$FAILED_TO_SEND_MESSAGE. $EXITING_REPEAT_LOOP")
                        return
                    }
                    Thread.sleep(FAIL_SLEEP_TIME)
                    pulsar = Pulsar(pulsarSettings, setUserFeedback) // Recreate producer so it can reconnect
                    setUserFeedback("$FAILED_TO_SEND_MESSAGE. $RETRYING. $FAILS:$fails")
                } else {
                    fails = 0
                }
                duration = System.currentTimeMillis() - startTime
                if (duration < delay) {
                    sleepTime = delay - duration
                    Thread.sleep(sleepTime)
                } else {
                    sleepTime = MIN_SLEEP_TIME
                    Thread.sleep(MIN_SLEEP_TIME)
                }
            }
            setUserFeedback(EXITING_REPEAT_LOOP)
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_SEND_MESSAGE. $EXITING_REPEAT_LOOP $ex")
        } finally {
            pulsar?.close()
            sendRepeatMessages = false
            runBlocking {
                job.complete()
                repeatingMessageJob = null
            }
        }
    }

    private fun createRepeatJob(): CompletableJob {
        sendRepeatMessages = true
        return Job().also { repeatingMessageJob = it }
    }

    private fun cancelRepeatJob() {
        runBlocking {
            sendRepeatMessages = false
            repeatingMessageJob?.let { job ->
                setUserFeedback(WAIT_SEND_TO_FINISH)
                job.join()
                setUserFeedback(CANCELLED_REPEAT_MESSAGES)
                repeatingMessageJob = null
            }
        }
    }

    private fun onRepeatSelected(isChecked: Boolean) {
        isRepeatSelected.value = isChecked
        if (isChecked) {
            sendButtonActiveText.value = STOP
        } else {
            sendButtonActiveText.value = SENDING
            sendRepeatMessages = false
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
                sendButtonActiveText = sendButtonActiveText.value,
                sendButtonIsCancellable = isRepeatSelected.value,
                generateClassTemplate = ::generateClassTemplate,
                compileMessage = ::compileMessage,
                sendPulsarMessage = ::sendPulsarMessage,
                scrollPane = sp,
                isRepeatSelected = isRepeatSelected.value,
                onRepeatSelected = ::onRepeatSelected,
                isRecompileSelected = isRecompileSelected.value,
                onRecompileSelected = isRecompileSelected::onStateChange,
                delay = delayString.value,
                onDelayChanged = delayString::onChangeDigits
            )
        }
    }

    companion object {
        private const val CANCEL_SCOPE_LOG = "Shutting down SendProtobufMessage"
        private const val MAX_FAILS = 5
        private const val FAIL_SLEEP_TIME = 1000L
        private const val MIN_SLEEP_TIME = 1L
    }
}
