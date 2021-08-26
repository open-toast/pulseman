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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.SelectedView
import com.toasttab.pulseman.state.Tab
import com.toasttab.pulseman.view.ViewUtils.styledTextField

/**
 * This holds all the views and settings form an individual tab.
 */
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun tabUI(state: Tab) {
    val padding = 4.dp
    Surface(
        Modifier.fillMaxSize(),
        color = AppTheme.colors.backgroundDark,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(color = AppTheme.colors.backgroundLight)
                    .border(2.dp, AppTheme.colors.backgroundMedium)
                    .padding(2.dp)
            ) {
                Column {
                    Row {
                        styledTextField(
                            "Tab Name",
                            state.tabName.value,
                            modifier = Modifier.padding(padding).fillMaxWidth(0.25F),
                            onValueChange = state::onTabNameChange
                        )
                    }

                    pulsarSettingsUI(state.pulsarSettings)
                }
            }

            Box(Modifier.weight(0.85F).padding(4.dp)) {
                Column {
                    selectTabViewUI(state)
                    Box(
                        Modifier
                            .background(color = AppTheme.colors.backgroundLight)
                            .padding(2.dp)
                    ) {
                        when (state.selectedView.value) {
                            SelectedView.SEND -> {
                                sendMessageUI(state.sendMessage)
                            }
                            SelectedView.RECEIVE -> {
                                receiveMessageUI(state.receiveMessage)
                            }
                            SelectedView.PROTOBUF_CLASS -> {
                                messageClassSelectorUI(state.protobufSelector)
                            }
                        }
                    }
                }
            }
            Box(Modifier.weight(0.15F)) {
                userFeedbackUI(state.userFeedback)
            }
        }
    }
}

@Composable
fun selectTabViewUI(state: Tab) {
    Row {
        listOf(
            Triple("Send", SelectedView.SEND) { state.selectedView.value = SelectedView.SEND },
            Triple("Receive", SelectedView.RECEIVE) { state.selectedView.value = SelectedView.RECEIVE },
            Triple("Class", SelectedView.PROTOBUF_CLASS) { state.selectedView.value = SelectedView.PROTOBUF_CLASS }
        ).forEach { selectedViewTab ->
            Surface(color = if (state.selectedView.value == selectedViewTab.second) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundMedium) {
                Row(
                    modifier = Modifier.clickable(onClick = selectedViewTab.third),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        selectedViewTab.first,
                        color = Color.White,
                        fontSize = 18.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
