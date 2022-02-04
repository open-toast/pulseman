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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.AppStrings.NEW_TAB
import com.toasttab.pulseman.entities.SelectedView
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValues
import com.toasttab.pulseman.view.selectTabViewUI
import com.toasttab.pulseman.view.tabUI

class TabState(
    appState: AppState,
    private val tabName: MutableState<String> = mutableStateOf(NEW_TAB),
    val selection: SingleSelection<TabState>,
    val close: ((TabState) -> Unit),
    val unsavedChanges: MutableState<Boolean> = mutableStateOf(false),
    val initialSettings: TabValues? = null,
    initialMessage: String? = null
) {
    private var lastSavedTabValues: TabValues? = initialSettings

    private val userFeedback = UserFeedback()

    private val protobufSelector =
        MessageClassSelector(
            pulsarMessageJars = appState.pulsarMessageJars,
            setUserFeedback = userFeedback::setUserFeedback,
            onChange = ::onChange,
            initialSettings = initialSettings
        )

    private val authSelector =
        AuthSelector(
            authJars = appState.authJars,
            setUserFeedback = userFeedback::setUserFeedback,
            initialSettings = initialSettings,
            onChange = ::onChange
        )

    private val propertySettings = PropertyConfiguration(::onChange, initialSettings)

    fun cleanUp() {
        receiveMessage.close()
        sendMessage.close()
        pulsarSettings.close()
    }

    fun tabValues(save: Boolean = false): TabValues {
        val tabValues = TabValues(
            tabName = tabName.value,
            topic = pulsarSettings.topic.value,
            serviceUrl = pulsarSettings.serviceUrl.value,
            code = sendMessage.currentCode(),
            selectedClassSend = protobufSelector.selectedSendClass.selected?.cls?.name,
            selectedClassReceive = protobufSelector.selectedReceiveClasses
                .mapNotNull { if (it.value) it.key.cls.name else null },
            selectedAuthClass = authSelector.selectedAuthClass.selected?.cls?.name,
            authJsonParameters = authSelector.authJsonParameters(),
            propertyMap = propertySettings.propertyMap()
        )

        if (save) {
            lastSavedTabValues = tabValues
        }

        return tabValues
    }

    init {
        initialSettings?.tabName?.let {
            tabName.value = it
        }
        initialMessage?.let {
            userFeedback.setUserFeedback(it)
        }
    }

    val isActive: Boolean
        get() = selection.selected === this

    fun activate() {
        selection.selected = this
    }

    private val pulsarSettings =
        PulsarSettings(
            appState = appState,
            setUserFeedback = userFeedback::setUserFeedback,
            initialSettings = initialSettings,
            protobufSelector = protobufSelector,
            authSelector = authSelector,
            propertySettings = propertySettings,
            onChange = ::onChange
        )

    private val selectedView = mutableStateOf(SelectedView.SEND)

    private val sendMessage = SendMessage(
        setUserFeedback = userFeedback::setUserFeedback,
        selectedClass = protobufSelector.selectedSendClass,
        pulsarSettings = pulsarSettings,
        initialSettings = initialSettings,
        onChange = ::onChange
    )
    private val receiveMessage = ReceiveMessage(
        setUserFeedback = userFeedback::setUserFeedback,
        pulsarSettings = pulsarSettings,
        selectedReceiveClasses = protobufSelector.selectedReceiveClasses
    )

    // Tab icon settings
    private val focused = mutableStateOf(false)
    private val image = mutableStateOf(Icons.Default.Circle)
    private val drawBackground = mutableStateOf(false)

    private fun onEnterIconUnsavedChanges() {
        drawBackground.value = true
        image.value = Icons.Default.Close
    }

    private fun onExitIconUnsavedChanges() {
        drawBackground.value = false
        image.value = Icons.Default.Circle
    }

    private fun onEnterIcon() {
        drawBackground.value = true
    }

    private fun onExitIcon() {
        drawBackground.value = false
    }

    private fun onFocusedUpdate(newValue: Boolean) {
        focused.value = newValue
    }

    private fun onTabNameChange(newValue: String) {
        tabName.value = newValue
        onChange()
    }

    private fun onChange() {
        unsavedChanges.value = lastSavedTabValues != tabValues()
    }

    @ExperimentalFoundationApi
    @Composable
    fun toTab(): Tab {
        return Tab(
            tabName = tabName.value,
            close = { close(this) },
            unsavedChanges = unsavedChanges.value,
            initialSettings = initialSettings,
            isActive = isActive,
            onFocusedUpdate = ::onFocusedUpdate,
            activate = ::activate,
            image = image.value,
            onEnterIconUnsavedChanges = ::onEnterIconUnsavedChanges,
            onExitIconUnsavedChanges = ::onExitIconUnsavedChanges,
            onEnterIcon = ::onEnterIcon,
            onExitIcon = ::onExitIcon,
            drawBackground = drawBackground.value,
            focused = focused.value,
            ui = {
                tabUI(
                    tabName = tabName.value,
                    onTabNameChange = ::onTabNameChange,
                    selectedView = selectedView.value,
                    userFeedbackUI = userFeedback.ui(),
                    messageClassSelectorUI = protobufSelector.getUI(),
                    pulsarSettingsUI = pulsarSettings.getUI(),
                    receiveMessageUI = receiveMessage.getUI(),
                    sendMessageUI = sendMessage.getUI(),
                    selectTabViewUI = {
                        selectTabViewUI(selectedView.value, selectedView::onStateChange)
                    }
                )
            }
        )
    }
}
