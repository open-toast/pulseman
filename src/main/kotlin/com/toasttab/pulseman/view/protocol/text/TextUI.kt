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

package com.toasttab.pulseman.view.protocol.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.state.protocol.text.SelectedTextView

/**
 * Groups all the tabs for sending and receiving a standard text based message. It contains the following tabs.
 * - Send message tab
 * - Receive message tab
 */
@Composable
fun textUI(
    selectedTextView: SelectedTextView,
    receiveMessageUI: @Composable () -> Unit,
    sendMessageUI: @Composable () -> Unit,
    selectSerializationUI: @Composable () -> Unit,
    selectTabViewUI: @Composable () -> Unit
) {
    Column {
        selectTabViewUI()
        Box(
            Modifier
                .background(color = AppTheme.colors.backgroundLight)
                .padding(2.dp)
        ) {
            when (selectedTextView) {
                SelectedTextView.SEND -> {
                    sendMessageUI()
                }
                SelectedTextView.RECEIVE -> {
                    receiveMessageUI()
                }
                SelectedTextView.SERIALIZATION -> {
                    selectSerializationUI()
                }
            }
        }
    }
}
