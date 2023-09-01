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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.DialogState
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.AppStrings.AUTH
import com.toasttab.pulseman.AppStrings.OTHER
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.view.propertyConfigurationUI
import com.toasttab.pulseman.view.pulsarSettingsUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class PulsarSettings(
    val appState: AppState,
    val topic: MutableState<String> = mutableStateOf(""),
    val pulsarAdminUrl: MutableState<String> = mutableStateOf(""),
    val serviceUrl: MutableState<String> = mutableStateOf(""),
    val authSelector: AuthSelector,
    val propertySettings: PropertyConfiguration,
    setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    initialSettings: TabValuesV3? = null
) {
    init {
        topic.value = initialSettings?.topic ?: ""
        serviceUrl.value = initialSettings?.serviceUrl ?: DEFAULT_SERVICE_URL
        pulsarAdminUrl.value = initialSettings?.pulsarAdminURL ?: DEFAULT_PULSAR_URL
    }

    fun close() {
        scope.cancel()
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val topicSelector = TopicSelector(
        pulsarSettings = this,
        settingsTopic = topic,
        pulsarAdminUrl = pulsarAdminUrl,
        setUserFeedback = setUserFeedback,
        onChange = onChange
    )

    private val authJarManagement =
        JarManagement(appState.authJars, authSelector.selectedAuthClass, setUserFeedback, onChange)
    private val dependencyJarManagement =
        JarManagement(appState.dependencyJars, null, setUserFeedback, onChange)

    private val showDiscover = mutableStateOf(false)
    private val showJarManagement = mutableStateOf(false)
    private val showAuthSettings = mutableStateOf(false)
    private val showPropertySettings = mutableStateOf(false)

    private val jarManagementTabs = JarManagementTabs(
        listOf(
            Pair(AUTH, authJarManagement),
            Pair(OTHER, dependencyJarManagement)
        )
    )

    @ExperimentalFoundationApi
    @Composable
    fun getUI(popupState: DialogState): @Composable () -> Unit {
        return {
            pulsarSettingsUI(
                popupState = popupState,
                topic = topic.value,
                onTopicChange = { topic.onStateChange(it, onChange) },
                showDiscover = showDiscover.value,
                onShowDiscoverChange = showDiscover::onStateChange,
                serviceUrl = serviceUrl.value,
                onServiceUrlChange = { serviceUrl.onStateChange(it, onChange) },
                showJarManagement = showJarManagement.value,
                onShowJarManagementChange = showJarManagement::onStateChange,
                showAuthSettings = showAuthSettings.value,
                onShowAuthSettingsChange = showAuthSettings::onStateChange,
                showPropertySettings = showPropertySettings.value,
                onShowPropertySettingsChange = showPropertySettings::onStateChange,
                topicSelectorUI = topicSelector.getUI(),
                jarManagementTabsUI = jarManagementTabs.getUI(),
                authSelectorUI = authSelector.getUI(),
                propertyConfigurationUI = {
                    propertyConfigurationUI(scrollPane = propertySettings.sp)
                }
            )
        }
    }

    companion object {
        private const val DEFAULT_SERVICE_URL = "pulsar://localhost:6650"
        private const val DEFAULT_PULSAR_URL = "http://localhost:8079"
    }
}
