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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.toasttab.pulseman.AppStrings.FAILED_TO_DESERIALIZE_PULSAR
import com.toasttab.pulseman.AppStrings.NO_CLASS_SELECTED_DESERIALIZE
import com.toasttab.pulseman.AppStrings.PROPERTIES
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import org.apache.pulsar.client.api.Message
import java.time.Instant

/**
 * This class handles printing received pulsar messages
 */
class MessageHandling(
    private val selectedReceiveClasses: SnapshotStateMap<PulsarMessage, Boolean>,
    private val receivedMessages: SnapshotStateList<ReceivedMessages>,
    private val setUserFeedback: (String) -> Unit
) {

    fun parseMessage(message: Message<ByteArray>) {
        try {
            val classes = selectedReceiveClasses.filter { it.value }.map { it.key }
            if (classes.isEmpty()) {
                setUserFeedback(NO_CLASS_SELECTED_DESERIALIZE)
                return
            }

            classes.forEachIndexed { index, it ->
                val indexString = if (classes.size > 1) " - $index" else ""
                val messageString = it.prettyPrint(it.deserialize(message.data))
                val publishTime = Instant.ofEpochMilli(message.publishTime)
                receivedMessages.add(
                    ReceivedMessages(
                        "$messageString\n$PROPERTIES:\n${message.properties}",
                        "$publishTime - ${it.cls.simpleName}$indexString",
                        mutableStateOf(false)
                    )
                )
            }
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_DESERIALIZE_PULSAR:$ex.")
        }
    }
}
