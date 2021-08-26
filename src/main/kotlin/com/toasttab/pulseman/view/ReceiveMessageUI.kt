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

package com.toasttab.pulseman.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.state.ReceiveMessage
import com.toasttab.pulseman.view.ViewUtils.threadedButton

/**
 * This views presents a list of the deserialized messages from the topic being monitored.
 *
 * TODO make the close connection button kill any currently attempting subscriptions
 */
@Composable
fun receiveMessageUI(state: ReceiveMessage) {
    Column {
        Row {
            threadedButton(state.scope, "Subscribing...", "Subscribe", state.subscribeState) {
                state.onSubscribe()
            }

            threadedButton(state.scope, "Clearing...", "Clear", state.clearState) {
                state.onClear()
            }

            threadedButton(state.scope, "Closing...", "Close Connection", state.closeState) {
                state.onCloseConnection()
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.verticalScroll(state.stateVertical)) {
                state.receivedMessages.forEach { receivedMessage ->
                    Card(
                        backgroundColor = AppTheme.colors.backgroundMedium,
                        border = BorderStroke(1.dp, AppTheme.colors.backgroundDark)
                    ) {
                        Column {
                            Row {
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = AnnotatedString(receivedMessage.header),
                                    modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { receivedMessage.expanded.value = !receivedMessage.expanded.value }
                                ) {
                                    if (receivedMessage.expanded.value)
                                        Icon(Icons.Default.ArrowDropUp, contentDescription = "Collapse")
                                    else
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand")
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            if (receivedMessage.expanded.value) {
                                Divider(color = AppTheme.colors.backgroundMedium, thickness = 2.dp)
                                Text(
                                    text = AnnotatedString(receivedMessage.message),
                                    modifier = Modifier.fillMaxWidth().background(AppTheme.colors.backgroundDark)
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(state.stateVertical)
            )
        }
    }
}
