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
import com.toasttab.pulseman.AppStrings.NO_CLASS_SELECTED_DESERIALIZE
import com.toasttab.pulseman.AppStrings.PROPERTIES
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import java.time.Instant
import org.apache.pulsar.client.api.Message

/**
 * This class handles printing received pulsar messages and logs the related class info
 */
class MessageHandlingClassImpl(
    private val selectedProtoClass: SingleSelection<PulsarMessageClassInfo>,
    private val receivedMessages: SnapshotStateList<ReceivedMessages>,
    private val setUserFeedback: (String) -> Unit
) : MessageHandling {

    override fun parseMessage(message: Message<ByteArray>) {
        try {
            val proto = selectedProtoClass.selected ?: run {
                setUserFeedback(NO_CLASS_SELECTED_DESERIALIZE)
                return
            }

            val messageString = proto.prettyPrint(proto.deserialize(message.data))
            val publishTime = Instant.ofEpochMilli(message.publishTime)
            if (receivedMessages.size > MAX_MESSAGES_STORED) {
                receivedMessages.removeFirst()
            }
            receivedMessages.add(
                ReceivedMessages(
                    "$messageString\n$PROPERTIES:\n${message.properties}",
                    "$publishTime - ${proto.cls.simpleName}",
                    mutableStateOf(false)
                )
            )
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_DESERIALIZE_PULSAR:$ex.")
        }
    }

    companion object {
        private const val MAX_MESSAGES_STORED = 500
    }
}
