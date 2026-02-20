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
import com.toasttab.pulseman.AppStrings.PROPERTY_FILTER

class PropertyFilter(
    initialFilters: List<String>,
    initialOptions: Map<String, String> = emptyMap()
) {
    val optionsState: MutableState<Map<String, String>> = mutableStateOf(initialOptions)

    private val selectedProperties: MutableState<Set<String>> = mutableStateOf(initialFilters.toSet())

    val filterState: MutableState<Map<String, String>> = mutableStateOf(
        computeFilter(initialFilters.toSet(), initialOptions)
    )

    val dropdown: MultiSelectDropdown = MultiSelectDropdown(
        label = PROPERTY_FILTER,
        options = { optionsState.value },
        selectedValues = { selectedProperties.value },
        onSelectionChanged = { selected ->
            selectedProperties.value = selected
            recomputeFilter()
        }
    )

    fun update(options: Map<String, String>) {
        optionsState.value = options
        recomputeFilter()
    }

    fun currentFilters(): List<String> = selectedProperties.value.toList()

    private fun recomputeFilter() {
        filterState.value = computeFilter(selectedProperties.value, optionsState.value)
    }

    private fun computeFilter(selected: Set<String>, options: Map<String, String>): Map<String, String> =
        selected.filter { options.containsKey(it) }.associateWith { options[it]!! }
}
