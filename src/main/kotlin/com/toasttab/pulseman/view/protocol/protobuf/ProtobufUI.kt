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

package com.toasttab.pulseman.view.protocol.protobuf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.state.protocol.protobuf.SelectedProtobufView

/**
 * Groups all the tabs for sending and receiving a protobuf message. It contains the following tabs.
 * - Send message tab
 * - Receive message tab
 * - Convert text to protobuf class tab
 * - Select protobuf jars tab
 * - Select protobuf serialization and deserialization class tab
 */
@Composable
fun protobufUI(
    selectedProtobufView: SelectedProtobufView,
    messageClassSelectorUI: @Composable () -> Unit,
    receiveMessageUI: @Composable () -> Unit,
    sendMessageUI: @Composable () -> Unit,
    selectTabViewUI: @Composable () -> Unit,
    protobufJarManagementUI: @Composable () -> Unit,
    gradleUI: @Composable () -> Unit,
    byteConversionUI: @Composable () -> Unit
) {
    Column {
        selectTabViewUI()
        Box(
            Modifier
                .background(color = AppTheme.colors.backgroundLight)
                .padding(2.dp)
        ) {
            when (selectedProtobufView) {
                SelectedProtobufView.SEND -> {
                    sendMessageUI()
                }

                SelectedProtobufView.RECEIVE -> {
                    receiveMessageUI()
                }

                SelectedProtobufView.BYTE_CONVERT -> {
                    byteConversionUI()
                }

                SelectedProtobufView.JAR_MANAGEMENT -> {
                    protobufJarManagementUI()
                }

                SelectedProtobufView.GRADLE -> {
                    gradleUI()
                }

                SelectedProtobufView.PROTOBUF_CLASS -> {
                    messageClassSelectorUI()
                }
            }
        }
    }
}
