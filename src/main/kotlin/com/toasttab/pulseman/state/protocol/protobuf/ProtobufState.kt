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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.TabValuesV2
import com.toasttab.pulseman.pulsar.MessageHandlingClassImpl
import com.toasttab.pulseman.state.JarManagement
import com.toasttab.pulseman.state.JarManagementTabs
import com.toasttab.pulseman.state.PulsarSettings
import com.toasttab.pulseman.state.ReceiveMessage
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.view.protocol.protobuf.protobufUI
import com.toasttab.pulseman.view.selectTabViewUI

class ProtobufState(
    appState: AppState,
    initialSettings: TabValuesV2? = null,
    pulsarSettings: PulsarSettings,
    setUserFeedback: (String) -> Unit,
    onChange: () -> Unit
) {
    fun cleanUp() {
        receiveMessage.close()
        sendMessage.close()
    }

    private val protobufSelector =
        ProtobufMessageClassSelector(
            pulsarMessageJars = appState.pulsarMessageJars,
            setUserFeedback = setUserFeedback,
            onChange = onChange,
            initialSettings = initialSettings
        )

    private val protobufJarManagement =
        JarManagement(
            appState.pulsarMessageJars,
            protobufSelector.selectedSendClass,
            setUserFeedback,
            onChange
        )

    private val protobufJarManagementTab = JarManagementTabs(
        listOf(
            Pair(AppStrings.MESSAGE, protobufJarManagement)
        )
    )

    private val sendMessage = SendProtobufMessage(
        appState = appState,
        setUserFeedback = setUserFeedback,
        selectedClass = protobufSelector.selectedSendClass,
        pulsarSettings = pulsarSettings,
        initialSettings = initialSettings,
        onChange = onChange
    )

    private val receivedMessages: SnapshotStateList<ReceivedMessages> = mutableStateListOf()
    private val messageHandling = MessageHandlingClassImpl(
        selectedReceiveClasses = protobufSelector.selectedReceiveClasses,
        receivedMessages = receivedMessages,
        setUserFeedback = setUserFeedback
    )

    private val receiveMessage = ReceiveMessage(
        setUserFeedback = setUserFeedback,
        pulsarSettings = pulsarSettings,
        receivedMessages = receivedMessages,
        messageHandling = messageHandling
    )

    fun toProtobufTabValues() =
        ProtobufTabValues(
            code = sendMessage.currentCode(),
            selectedClassSend = protobufSelector.selectedSendClass.selected?.cls?.name,
            selectedClassReceive = protobufSelector.selectedReceiveClasses
                .mapNotNull { if (it.value) it.key.cls.name else null }
        )

    private val selectedView = mutableStateOf(SelectedProtobufView.SEND)

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            protobufUI(
                selectedProtobufView = selectedView.value,
                messageClassSelectorUI = protobufSelector.getUI(),
                receiveMessageUI = receiveMessage.getUI(),
                sendMessageUI = sendMessage.getUI(),
                selectTabViewUI = {
                    selectTabViewUI(
                        listOf(
                            Triple(AppStrings.SEND, SelectedProtobufView.SEND) {
                                selectedView.onStateChange(SelectedProtobufView.SEND)
                            },
                            Triple(AppStrings.RECEIVE, SelectedProtobufView.RECEIVE) {
                                selectedView.onStateChange(SelectedProtobufView.RECEIVE)
                            },
                            Triple(AppStrings.JARS, SelectedProtobufView.JAR_MANAGEMENT) {
                                selectedView.onStateChange(SelectedProtobufView.JAR_MANAGEMENT)
                            },
                            Triple(AppStrings.CLASS, SelectedProtobufView.PROTOBUF_CLASS) {
                                selectedView.onStateChange(SelectedProtobufView.PROTOBUF_CLASS)
                            }
                        ),
                        selectedView.value
                    )
                },
                protobufJarManagementUI = protobufJarManagementTab.getUI()
            )
        }
    }
}
