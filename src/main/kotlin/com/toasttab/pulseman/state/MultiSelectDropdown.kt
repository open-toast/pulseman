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
    private val options: () -> Map<String, String>,
    private val selectedValues: () -> Set<String>,
    private val onSelectionChanged: (Set<String>) -> Unit
) {
    private val expanded = mutableStateOf(false)

    fun getUI(): @Composable () -> Unit = {
        multiSelectDropdownUI(
            label = label,
            expanded = expanded.value,
            onChangeExpanded = { expanded.value = !expanded.value },
            options = options(),
            selectedValues = selectedValues(),
            onSelectionChanged = onSelectionChanged
        )
    }
}
