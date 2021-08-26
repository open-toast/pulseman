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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.jars.RunTimeJarLoader.addJarsToClassLoader
import com.toasttab.pulseman.pulsar.MessageHandling
import com.toasttab.pulseman.pulsar.Pulsar
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.apache.pulsar.client.api.Consumer
import java.util.concurrent.TimeUnit

class ReceiveMessage(
    private val setUserFeedback: (String) -> Unit,
    private val pulsarSettings: PulsarSettings,
    selectedReceiveClasses: SnapshotStateMap<PulsarMessage, Boolean>
) {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val subscribeState = mutableStateOf(ButtonState.WAITING)
    val clearState = mutableStateOf(ButtonState.WAITING)
    val closeState = mutableStateOf(ButtonState.WAITING)

    val receivedMessages: SnapshotStateList<ReceivedMessages> = mutableStateListOf()
    private val messageHandling = MessageHandling(selectedReceiveClasses, receivedMessages, setUserFeedback)

    private val pulsar: MutableState<Pulsar?> = mutableStateOf(null)
    private var consumer: Consumer<ByteArray>? = null

    val stateVertical = ScrollState(0)

    fun onSubscribe() {
        consumer?.close()
        pulsar.value?.close()
        pulsar.value = Pulsar(pulsarSettings, setUserFeedback)
        try {
            addJarsToClassLoader()
            val subscribeFuture = pulsar.value?.createNewConsumer(messageHandling::parseMessage)
            subscribeFuture?.get(90, TimeUnit.SECONDS)?.let {
                consumer = it
                setUserFeedback("Subscribed")
            }
        } catch (ex: Throwable) {
            pulsar.value?.close()
            setUserFeedback("Fail to subscribe:$ex")
        }
    }

    fun onClear() {
        receivedMessages.clear()
        setUserFeedback("Cleared history")
    }

    fun onCloseConnection() {
        consumer?.close()
        pulsar.value?.close()
        setUserFeedback("Connection closed")
    }

    fun close() {
        consumer?.close()
        pulsar.value?.close()
        scope.cancel("Shutting down ReceiveMessage")
    }
}
