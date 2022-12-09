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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import com.toasttab.pulseman.AppStrings.AUTH_SETTINGS
import com.toasttab.pulseman.AppStrings.JARS
import com.toasttab.pulseman.AppStrings.MANAGE_JARS
import com.toasttab.pulseman.AppStrings.PROPERTIES
import com.toasttab.pulseman.AppStrings.SEARCH_FOR_TOPIC
import com.toasttab.pulseman.AppStrings.SELECT_TOPIC_UNSECURED
import com.toasttab.pulseman.AppStrings.SERVICE_URL
import com.toasttab.pulseman.AppStrings.SET_AUTHORIZATION_VALUES
import com.toasttab.pulseman.AppStrings.SET_MESSAGE_PROPERTY_VALUES
import com.toasttab.pulseman.AppStrings.TOPIC
import com.toasttab.pulseman.view.ViewUtils.styledTextField

/**
 * This view collects the multiple configuration settings in the top half of the tab.
 */
@Composable
fun pulsarSettingsUI(
    popupState: DialogState,
    topic: String,
    onTopicChange: (String) -> Unit,
    showDiscover: Boolean,
    onShowDiscoverChange: (Boolean) -> Unit,
    serviceUrl: String,
    onServiceUrlChange: (String) -> Unit,
    showJarManagement: Boolean,
    onShowJarManagementChange: (Boolean) -> Unit,
    showAuthSettings: Boolean,
    onShowAuthSettingsChange: (Boolean) -> Unit,
    showPropertySettings: Boolean,
    onShowPropertySettingsChange: (Boolean) -> Unit,
    topicSelectorUI: @Composable () -> Unit,
    jarManagementTabsUI: @Composable () -> Unit,
    authSelectorUI: @Composable () -> Unit,
    propertyConfigurationUI: @Composable () -> Unit
) {
    val padding = 4.dp
    Column {
        Row {
            styledTextField(
                TOPIC,
                topic,
                modifier = Modifier.weight(1F).padding(padding),
                onValueChange = onTopicChange
            )

            IconButton(
                onClick = { onShowDiscoverChange(true) },
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Search, contentDescription = SEARCH_FOR_TOPIC)
            }
            if (showDiscover) {
                Dialog(
                    onCloseRequest = { onShowDiscoverChange(false) },
                    title = SELECT_TOPIC_UNSECURED,
                    state = popupState
                ) {
                    topicSelectorUI()
                }
            }
        }
        Row {
            styledTextField(
                SERVICE_URL,
                serviceUrl,
                modifier = Modifier.weight(1F).padding(padding),
                onValueChange = onServiceUrlChange
            )
        }
        Row {
            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { onShowJarManagementChange(true) },
            ) {
                Text(JARS)
            }
            if (showJarManagement) {
                Dialog(
                    onCloseRequest = { onShowJarManagementChange(false) },
                    title = MANAGE_JARS,
                    state = popupState
                ) {
                    jarManagementTabsUI()
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { onShowAuthSettingsChange(true) },
            ) {
                Text(AUTH_SETTINGS)
            }
            if (showAuthSettings) {
                Dialog(
                    onCloseRequest = { onShowAuthSettingsChange(false) },
                    title = SET_AUTHORIZATION_VALUES,
                    state = popupState
                ) {
                    authSelectorUI()
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { onShowPropertySettingsChange(true) },
            ) {
                Text(PROPERTIES)
            }
            if (showPropertySettings) {
                Dialog(
                    onCloseRequest = { onShowPropertySettingsChange(false) },
                    title = SET_MESSAGE_PROPERTY_VALUES,
                    state = popupState
                ) {
                    propertyConfigurationUI()
                }
            }
        }
    }
}
