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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.entities.SelectedView
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValues

class Tab(
    appState: AppState,
    val tabName: MutableState<String> = mutableStateOf("New tab"),
    val selection: SingleSelection<Tab>,
    val close: ((Tab) -> Unit),
    val unsavedChanges: MutableState<Boolean> = mutableStateOf(false),
    val initialSettings: TabValues? = null,
) {
    private var lastSavedTabValues: TabValues? = initialSettings

    val userFeedback = UserFeedback()

    val protobufSelector =
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
            onChange = { onChange() }
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
    }

    val isActive: Boolean
        get() = selection.selected === this

    fun activate() {
        selection.selected = this
    }

    val pulsarSettings =
        PulsarSettings(
            appState = appState,
            setUserFeedback = userFeedback::setUserFeedback,
            initialSettings = initialSettings,
            protobufSelector = protobufSelector,
            authSelector = authSelector,
            propertySettings = propertySettings,
            onChange = ::onChange
        )

    val selectedView = mutableStateOf(SelectedView.SEND)

    val sendMessage = SendMessage(
        setUserFeedback = userFeedback::setUserFeedback,
        selectedClass = protobufSelector.selectedSendClass,
        pulsarSettings = pulsarSettings,
        initialSettings = initialSettings,
        onChange = { onChange() }
    )
    val receiveMessage = ReceiveMessage(
        setUserFeedback = userFeedback::setUserFeedback,
        pulsarSettings = pulsarSettings,
        selectedReceiveClasses = protobufSelector.selectedReceiveClasses
    )

    // Tab icon settings
    val focused = mutableStateOf(false)
    val image = mutableStateOf(Icons.Default.Circle)
    val drawBackground = mutableStateOf(false)

    fun onEnterIconUnsavedChanges() {
        drawBackground.value = true
        image.value = Icons.Default.Close
    }

    fun onExitIconUnsavedChanges() {
        drawBackground.value = false
        image.value = Icons.Default.Circle
    }

    fun onEnterIcon() {
        drawBackground.value = true
    }

    fun onExitIcon() {
        drawBackground.value = false
    }

    fun onFocusedUpdate(newValue: Boolean) {
        focused.value = newValue
    }

    fun onTabNameChange(newValue: String) {
        tabName.value = newValue
        onChange()
    }

    private fun onChange() {
        unsavedChanges.value = lastSavedTabValues != tabValues()
    }
}
