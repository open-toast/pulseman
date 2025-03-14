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
import androidx.compose.ui.window.DialogState
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.AppStrings.NEW_TAB
import com.toasttab.pulseman.AppStrings.SELECTED
import com.toasttab.pulseman.AppStrings.SERIALIZATION_FORMAT
import com.toasttab.pulseman.entities.SerializationFormat
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.view.tabUI
import java.util.UUID

class TabState(
    private val appState: AppState,
    private val tabName: MutableState<String> = mutableStateOf(NEW_TAB),
    val selection: SingleSelection<TabState>,
    val close: ((TabState) -> Unit),
    val unsavedChanges: MutableState<Boolean> = mutableStateOf(false),
    val initialSettings: TabValuesV3? = null,
    val tabID: UUID = UUID.randomUUID(),
    newTab: Boolean,
    initialMessage: String? = null,
    newJarFormat: Boolean
) {
    private val pulsarMessageJars = appState.tabJarManager.add(
        tabID = tabID,
        newJarFormat = newJarFormat,
        tabFileExtension = initialSettings?.tabExtension
    )

    private var lastSavedTabValues: TabValuesV3? = initialSettings

    private val userFeedback = UserFeedback(globalFeedback = appState.globalFeedback, newTab = newTab)

    private val serializationFormat =
        mutableStateOf((initialSettings?.serializationFormat ?: SerializationFormat.PROTOBUF))

    private val serializationFormatSelector = DropdownSelector(
        options = SerializationFormat.entries.map { it.format },
        onSelected = {
            serializationFormat.value = SerializationFormat.fromFormat(it)
            userFeedback.set("$SELECTED $it $SERIALIZATION_FORMAT")
            onChange()
        }
    )

    private val authSelector =
        AuthSelector(
            authJars = appState.authJars,
            setUserFeedback = userFeedback::set,
            initialSettings = initialSettings,
            onChange = ::onChange
        )

    private val propertySettings = PropertyConfiguration(::onChange, initialSettings)

    private val pulsarSettings =
        PulsarSettings(
            appState = appState,
            setUserFeedback = userFeedback::set,
            initialSettings = initialSettings,
            authSelector = authSelector,
            propertySettings = propertySettings,
            onChange = ::onChange
        )

    private val serializationState = SerializationState(
        pulsarMessageJars = pulsarMessageJars,
        initialSettings = initialSettings,
        pulsarSettings = pulsarSettings,
        setUserFeedback = userFeedback::set,
        onChange = ::onChange
    )

    fun cleanUp() {
        appState.tabJarManager.remove(tabID = tabID)
        serializationState.cleanUp()
        userFeedback.close()
    }

    fun tabValues(save: Boolean = false): TabValuesV3 {
        val tabValues = TabValuesV3(
            tabName = tabName.value,
            topic = pulsarSettings.topic.value,
            serviceUrl = pulsarSettings.serviceUrl.value,
            selectedAuthClass = authSelector.selectedAuthClass.selected?.cls?.name,
            authJsonParameters = authSelector.authJsonParameters(),
            propertyMap = propertySettings.propertyMap(),
            serializationFormat = serializationFormat.value,
            protobufSettings = serializationState.protobufState.toProtobufTabValues(),
            textSettings = serializationState.textState.toTextTabValues(),
            pulsarAdminURL = pulsarSettings.pulsarAdminUrl.value,
            tabExtension = pulsarMessageJars.tabFileExtension
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
            userFeedback.set(it)
        }
    }

    val isActive: Boolean
        get() = selection.selected === this

    fun activate() {
        selection.selected = this
    }

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

    private fun onChange() {
        unsavedChanges.value = lastSavedTabValues != tabValues()
    }

    @ExperimentalFoundationApi
    @Composable
    fun toTab(popupState: DialogState): Tab {
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
                    onTabNameChange = { tabName.onStateChange(it, ::onChange) },
                    userFeedbackUI = userFeedback.ui(),
                    pulsarSettingsUI = pulsarSettings.getUI(
                        popupState = popupState
                    ),
                    protocolUI = serializationState.getUI(
                        serializationFormat = serializationFormat.value
                    ),
                    serializationFormatSelectorUI = serializationFormatSelector.getUI(
                        currentlySelected = serializationFormat.value.format
                    )
                )
            }
        )
    }
}
