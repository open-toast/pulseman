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

import androidx.compose.runtime.mutableStateListOf
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.AppStrings.COPY
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValuesV3

class TabHolder(private val appState: AppState) {
    private val selection = SingleSelection<TabState>()
    val active: TabState? get() = selection.selected
    val tabState = mutableStateListOf<TabState>()

    fun allTabValues() = tabState.map { it.tabValues(save = true) }

    fun open(initialMessage: String?) {
        val tab = TabState(
            appState = appState,
            selection = selection,
            close = ::close,
            initialMessage = initialMessage,
            newTab = true
        )
        tabState.add(tab)
        tab.activate()
    }

    fun savedChanges() {
        tabState.forEach { it.unsavedChanges.value = false }
    }

    fun hasUnsavedChanges(): Boolean {
        return tabState.any { it.unsavedChanges.value }
    }

    fun load(tabSettings: List<TabValuesV3>) {
        tabSettings.forEach {
            val tab = TabState(
                appState = appState,
                selection = selection,
                close = ::close,
                initialSettings = it,
                newTab = false
            )
            tabState.add(tab)
        }
        tabState.firstOrNull()?.activate()
    }

    fun copyCurrentTab() {
        selection.selected?.let { currentTab ->
            val copiedTab = TabState(
                appState = appState,
                selection = selection,
                close = ::close,
                initialSettings = currentTab.tabValues().let { currentTabValues ->
                    currentTabValues.copy(
                        tabName = currentTabValues.tabName + " - $COPY"
                    )
                },
                newTab = true
            )
            tabState.add(copiedTab)
            copiedTab.activate()
        }
    }

    fun closeAll() {
        tabState.forEach { it.cleanUp() }
        tabState.clear()
        selection.selected = null
    }

    private fun close(tab: TabState) {
        val index = tabState.indexOf(tab)
        tabState.remove(tab)
        tab.cleanUp()
        if (tab.isActive) {
            selection.selected = tabState.getOrNull(index.coerceAtMost(tabState.lastIndex))
        }
    }
}
