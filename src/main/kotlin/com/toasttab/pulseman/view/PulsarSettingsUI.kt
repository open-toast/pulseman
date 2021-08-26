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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.toasttab.pulseman.state.PulsarSettings
import com.toasttab.pulseman.state.PulsarSettings.Companion.popupState
import com.toasttab.pulseman.view.ViewUtils.styledTextField

/**
 * This view collects the multiple configuration settings in the top half of the tab.
 */
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@Composable
fun pulsarSettingsUI(state: PulsarSettings) {
    val padding = 4.dp
    Column {
        Row {
            styledTextField(
                "Topic",
                state.topic.value,
                modifier = Modifier.weight(1F).padding(padding),
                onValueChange = state::onTopicChange
            )

            IconButton(
                onClick = { state.onShowDiscoverChange(true) },
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search for topic")
            }
            if (state.showDiscover.value) {
                Dialog(
                    onCloseRequest = { state.showDiscover.value = false },
                    title = "Select topic - unsecured",
                    state = popupState
                ) {
                    topicSelectorUI(state.topicSelector)
                }
            }
        }
        Row {
            styledTextField(
                "Service Url",
                state.serviceUrl.value,
                modifier = Modifier.weight(1F).padding(padding),
                onValueChange = state::onServiceUrlChange
            )
        }
        Row {
            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { state.onShowJarManagementChange(true) },
            ) {
                Text("Jars")
            }
            if (state.showJarManagement.value) {
                Dialog(
                    onCloseRequest = { state.onShowJarManagementChange(false) },
                    title = "Manage configurations jars",
                    state = popupState
                ) {
                    jarManagementTabsUI(state.jarManagementTabs)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { state.onShowAuthSettingsChange(true) },
            ) {
                Text("Auth Settings")
            }
            if (state.showAuthSettings.value) {
                Dialog(
                    onCloseRequest = { state.onShowAuthSettingsChange(false) },
                    title = "Set Authorization Values",
                    state = popupState
                ) {
                    authSelectorUI(state.authSelector)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { state.onShowPropertySettingsChange(true) },
            ) {
                Text("Properties")
            }
            if (state.showPropertySettings.value) {
                Dialog(
                    onCloseRequest = { state.onShowPropertySettingsChange(false) },
                    title = "Set message property values",
                    state = popupState
                ) {
                    propertyConfigurationUI(state.propertySettings)
                }
            }
        }
    }
}
