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
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValues

class TabHolder(private val appState: AppState) {
    private val selection = SingleSelection<Tab>()
    val active: Tab? get() = selection.selected
    val tabs = mutableStateListOf<Tab>()

    fun allTabValues() = tabs.map { it.tabValues(save = true) }

    fun open() {
        val tab = Tab(
            appState = appState,
            selection = selection,
            close = ::close
        )
        tabs.add(tab)
        tab.activate()
    }

    fun savedChanges() {
        tabs.forEach { it.unsavedChanges.value = false }
    }

    fun load(tabSettings: List<TabValues>) {
        tabSettings.forEach {
            val tab = Tab(
                appState = appState,
                selection = selection,
                close = ::close,
                initialSettings = it
            )
            tabs.add(tab)
        }
        tabs.firstOrNull()?.activate()
    }

    fun copyCurrentTab() {
        selection.selected?.let { currentTab ->
            val copiedTab = Tab(
                appState = appState,
                selection = selection,
                close = ::close,
                initialSettings = currentTab.tabValues().let { currentTabValues ->
                    currentTabValues.copy(
                        tabName = currentTabValues.tabName + " - copy"
                    )
                }
            )
            tabs.add(copiedTab)
            copiedTab.activate()
        }
    }

    fun closeAll() {
        tabs.forEach { it.cleanUp() }
        tabs.clear()
        selection.selected = null
    }

    private fun close(tab: Tab) {
        val index = tabs.indexOf(tab)
        tabs.remove(tab)
        tab.cleanUp()
        if (tab.isActive) {
            selection.selected = tabs.getOrNull(index.coerceAtMost(tabs.lastIndex))
        }
    }
}
