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

package com.toasttab.pulseman.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowSize
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.entities.TabValues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class PulsarSettings(
    val appState: AppState,
    val topic: MutableState<String> = mutableStateOf(""),
    val serviceUrl: MutableState<String> = mutableStateOf(""),
    val protobufSelector: MessageClassSelector,
    val authSelector: AuthSelector,
    val propertySettings: PropertyConfiguration,
    setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    initialSettings: TabValues? = null,
) {
    init {
        topic.value = initialSettings?.topic ?: ""
        serviceUrl.value = initialSettings?.serviceUrl ?: "pulsar://localhost:6650"
    }

    fun close() {
        scope.cancel()
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val topicSelector = TopicSelector(
        settingsTopic = topic,
        setUserFeedback = setUserFeedback
    )

    private val pulsarJarManagement =
        JarManagement(appState.pulsarMessageJars, protobufSelector.selectedSendClass, setUserFeedback, onChange)
    private val authJarManagement =
        JarManagement(appState.authJars, authSelector.selectedAuthClass, setUserFeedback, onChange)
    private val dependencyJarManagement =
        JarManagement(appState.dependencyJars, null, setUserFeedback, onChange)

    val showDiscover = mutableStateOf(false)
    val showJarManagement = mutableStateOf(false)
    val showAuthSettings = mutableStateOf(false)
    val showPropertySettings = mutableStateOf(false)

    fun onShowDiscoverChange(newValue: Boolean) {
        showDiscover.value = newValue
    }

    fun onShowJarManagementChange(newValue: Boolean) {
        showJarManagement.value = newValue
    }

    fun onShowAuthSettingsChange(newValue: Boolean) {
        showAuthSettings.value = newValue
    }

    fun onShowPropertySettingsChange(newValue: Boolean) {
        showPropertySettings.value = newValue
    }

    fun onTopicChange(newValue: String) {
        topic.value = newValue
        onChange()
    }

    fun onServiceUrlChange(newValue: String) {
        serviceUrl.value = newValue
        onChange()
    }

    val jarManagementTabs = JarManagementTabs(
        listOf(
            Pair("Message", pulsarJarManagement),
            Pair("Auth", authJarManagement),
            Pair("Other", dependencyJarManagement)
        )
    )

    companion object {
        val popupState = DialogState().apply {
            size = WindowSize(750.dp, 600.dp)
            position = WindowPosition.Aligned(Alignment.Center)
        }
    }
}
