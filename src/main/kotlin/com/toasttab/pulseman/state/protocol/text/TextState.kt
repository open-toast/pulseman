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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.pulsar.MessageHandlingImpl
import com.toasttab.pulseman.state.PulsarSettings
import com.toasttab.pulseman.state.ReceiveMessage
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.view.protocol.text.textUI
import com.toasttab.pulseman.view.selectTabViewUI

class TextState(
    initialSettings: TabValuesV3? = null,
    pulsarSettings: PulsarSettings,
    setUserFeedback: (String) -> Unit,
    onChange: () -> Unit
) {
    fun cleanUp() {
        receiveMessage.close()
        sendMessage.close()
    }

    private val serializationTypeSelector =
        SerializationTypeSelector(
            setUserFeedback = setUserFeedback,
            onChange = onChange,
            initialSettings = initialSettings
        )

    private val sendMessage = SendText(
        serializationTypeSelector = serializationTypeSelector,
        setUserFeedback = setUserFeedback,
        pulsarSettings = pulsarSettings,
        initialSettings = initialSettings,
        onChange = onChange
    )

    private val receivedMessages: SnapshotStateList<ReceivedMessages> = mutableStateListOf()

    private val messageHandling = MessageHandlingImpl(
        messageType = serializationTypeSelector.selectedEncoding,
        receivedMessages = receivedMessages,
        setUserFeedback = setUserFeedback
    )

    private val receiveMessage = ReceiveMessage(
        setUserFeedback = setUserFeedback,
        pulsarSettings = pulsarSettings,
        receivedMessages = receivedMessages,
        messageHandling = messageHandling
    )

    fun toTextTabValues() = TextTabValuesV3(
        text = sendMessage.currentText(),
        selectedEncoding = serializationTypeSelector.selectedCharacterSet?.charSet
    )

    private val selectedView = mutableStateOf(SelectedTextView.SEND)

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            textUI(
                selectedTextView = selectedView.value,
                receiveMessageUI = receiveMessage.getUI(),
                sendMessageUI = sendMessage.getUI(),
                selectSerializationUI = serializationTypeSelector.getUI(),
                selectTabViewUI = {
                    selectTabViewUI(
                        listOf(
                            Triple(AppStrings.SEND, SelectedTextView.SEND) {
                                selectedView.onStateChange(SelectedTextView.SEND)
                            },
                            Triple(AppStrings.RECEIVE, SelectedTextView.RECEIVE) {
                                selectedView.onStateChange(SelectedTextView.RECEIVE)
                            },
                            Triple(AppStrings.SERIALIZATION, SelectedTextView.SERIALIZATION) {
                                selectedView.onStateChange(SelectedTextView.SERIALIZATION)
                            }
                        ),
                        selectedView.value
                    )
                }
            )
        }
    }
}
