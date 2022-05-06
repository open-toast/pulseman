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

package com.toasttab.pulseman.state

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.AppStrings.CLEARED_HISTORY
import com.toasttab.pulseman.AppStrings.CONNECTION_CLOSED
import com.toasttab.pulseman.AppStrings.FAIL_TO_SUBSCRIBE
import com.toasttab.pulseman.AppStrings.SUBSCRIBED
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.jars.RunTimeJarLoader.addJarsToClassLoader
import com.toasttab.pulseman.pulsar.MessageHandling
import com.toasttab.pulseman.pulsar.Pulsar
import com.toasttab.pulseman.view.receiveMessageUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.apache.pulsar.client.api.Consumer
import java.util.concurrent.TimeUnit

class ReceiveMessage(
    private val setUserFeedback: (String) -> Unit,
    private val pulsarSettings: PulsarSettings,
    private val receivedMessages: SnapshotStateList<ReceivedMessages>,
    private val messageHandling: MessageHandling
) {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val subscribeState = mutableStateOf(ButtonState.WAITING)
    private val clearState = mutableStateOf(ButtonState.WAITING)
    private val closeState = mutableStateOf(ButtonState.WAITING)

    private val pulsar: MutableState<Pulsar?> = mutableStateOf(null)
    private var consumer: Consumer<ByteArray>? = null

    private val stateVertical = ScrollState(0)

    private fun onSubscribe() {
        consumer?.close()
        pulsar.value?.close()
        pulsar.value = Pulsar(pulsarSettings, setUserFeedback)
        try {
            addJarsToClassLoader()
            val subscribeFuture = pulsar.value?.createNewConsumer(messageHandling::parseMessage)
            subscribeFuture?.get(90, TimeUnit.SECONDS)?.let {
                consumer = it
                setUserFeedback(SUBSCRIBED)
            }
        } catch (ex: Throwable) {
            pulsar.value?.close()
            setUserFeedback("$FAIL_TO_SUBSCRIBE:$ex")
        }
    }

    private fun onClear() {
        receivedMessages.clear()
        setUserFeedback(CLEARED_HISTORY)
    }

    private fun onCloseConnection() {
        consumer?.close()
        pulsar.value?.close()
        setUserFeedback(CONNECTION_CLOSED)
    }

    fun close() {
        consumer?.close()
        pulsar.value?.close()
        scope.cancel(CANCEL_SCOPE_LOG)
    }

    fun getUI(): @Composable () -> Unit {
        return {
            receiveMessageUI(
                scope = scope,
                subscribeState = subscribeState.value,
                onSubscribeStateChange = subscribeState::onStateChange,
                clearState = clearState.value,
                onClearStateChange = clearState::onStateChange,
                closeState = closeState.value,
                onCloseStateChange = closeState::onStateChange,
                onSubscribe = ::onSubscribe,
                onClear = ::onClear,
                onCloseConnection = ::onCloseConnection,
                receivedMessages = receivedMessages,
                scrollState = stateVertical
            )
        }
    }

    companion object {
        private const val CANCEL_SCOPE_LOG = "Shutting down ReceiveMessage"
    }
}
