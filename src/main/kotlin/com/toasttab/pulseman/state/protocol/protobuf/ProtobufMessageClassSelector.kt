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

package com.toasttab.pulseman.state.protocol.protobuf

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.view.messageClassSelectorUI

class ProtobufMessageClassSelector(
    private val pulsarMessageJars: JarManager<PulsarMessageClassInfo>,
    val selectedClass: SingleSelection<PulsarMessageClassInfo> = SingleSelection(),
    val filter: MutableState<String> = mutableStateOf(""),
    private val listState: LazyListState = LazyListState(),
    val setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    initialSettings: TabValuesV3?
) {

    init {
        initialSettings?.protobufSettings?.selectedClass?.let { savedSelection ->
            selectedClass.selected = pulsarMessageJars.loadedClasses.getClass(savedSelection)
        }
    }

    private fun onSelectedClass(newValue: PulsarMessageClassInfo) {
        selectedClass.selected = newValue
        setUserFeedback("${AppStrings.SELECTED} ${newValue.cls.name}")
        onChange()
    }

    private fun filteredClasses() = pulsarMessageJars.loadedClasses.filter(filter.value)

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            messageClassSelectorUI(
                filter = filter.value,
                onFilterChange = filter::onStateChange,
                filteredClasses = filteredClasses(),
                onSelectedClass = ::onSelectedClass,
                selectedClass = selectedClass.selected,
                listState = listState
            )
        }
    }
}
