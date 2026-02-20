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

package com.toasttab.pulseman.pulsar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.AppStrings.FAILED_TO_DESERIALIZE_PULSAR
import com.toasttab.pulseman.AppStrings.PROPERTIES
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import org.apache.pulsar.client.api.Message
import java.time.Instant

/**
 * This class handles printing received pulsar messages
 */
class MessageHandlingImpl(
    private val messageType: SingleSelection<PulsarMessage>,
    private val propertyFilter: () -> Map<String, String>,
    private val receivedMessages: SnapshotStateList<ReceivedMessages>,
    private val setUserFeedback: (String) -> Unit
) : MessageHandling {

    private val _skippedMessages = mutableStateOf(0)
    override val skippedMessages: Int get() = _skippedMessages.value
    override fun resetSkippedMessages() { _skippedMessages.value = 0 }

    override fun parseMessage(message: Message<ByteArray>) {
        try {
            val currentFilter = propertyFilter()
            if (skipMessage(message, currentFilter)) {
                _skippedMessages.value++
                return
            }
            val messageString = messageType.selected?.deserialize(message.data)
            val publishTime = Instant.ofEpochMilli(message.publishTime)
            receivedMessages.add(
                ReceivedMessages(
                    "$messageString\n$PROPERTIES:\n${message.properties}",
                    "$publishTime",
                    mutableStateOf(false)
                )
            )
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_DESERIALIZE_PULSAR:$ex.")
        }
    }
}
