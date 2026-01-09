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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.view.multiSelectDropdownUI

class MultiSelectDropdown(
    private val label: String,
    private val optionsProvider: () -> Map<String, String>,
    private val selectedItems: () -> Set<String>,
    private val onSelectionChanged: (Map<String, String>) -> Unit
) {
    private val expanded = mutableStateOf(false)

    fun getUI(): @Composable () -> Unit {
        return {
            val currentOptions = optionsProvider()
            val currentSelectedKeys = selectedItems()

            // Clean up selected items that no longer exist in options
            val validSelectedKeys = currentSelectedKeys.filter { it in currentOptions.keys }
            if (validSelectedKeys.size != currentSelectedKeys.size) {
                // Notify about the cleaned selection
                val selectedMap = validSelectedKeys.associateWith { key ->
                    currentOptions[key] ?: ""
                }.filterValues { it.isNotEmpty() }
                onSelectionChanged(selectedMap)
            }

            multiSelectDropdownUI(
                label = label,
                expanded = expanded.value,
                selectedKeys = validSelectedKeys.toSet(),
                options = currentOptions,
                onChangeExpanded = { expanded.value = !expanded.value },
                onSelectionChanged = { selectedKeys ->
                    val selectedMap = selectedKeys.associateWith { key ->
                        currentOptions[key] ?: ""
                    }.filterValues { it.isNotEmpty() }
                    onSelectionChanged(selectedMap)
                }
            )
        }
    }
}
