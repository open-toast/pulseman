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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValues
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import com.toasttab.pulseman.view.messageClassSelectorUI

class MessageClassSelector(
    private val pulsarMessageJars: JarManager<PulsarMessage>,
    val selectedSendClass: SingleSelection<PulsarMessage> = SingleSelection(),
    val selectedReceiveClasses: SnapshotStateMap<PulsarMessage, Boolean> = mutableStateMapOf(),
    val filter: MutableState<String> = mutableStateOf(""),
    private val listState: LazyListState = LazyListState(),
    val setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    initialSettings: TabValues?
) {

    init {
        initialSettings?.selectedClassSend?.let { savedSelection ->
            selectedSendClass.selected = pulsarMessageJars.loadedClasses.getClass(savedSelection)
        }
        initialSettings?.selectedClassReceive?.forEach { savedSelection ->
            pulsarMessageJars.loadedClasses.getClass(savedSelection)?.let {
                selectedReceiveClasses[it] = true
            }
        }
    }

    private fun onSelectedSendClass(newValue: PulsarMessage) {
        selectedSendClass.selected = newValue
    }

    private fun onSelectedReceiveClass(newValue: PulsarMessage) {
        selectedReceiveClasses[newValue] = selectedReceiveClasses[newValue] != true
    }

    private fun filteredClasses() = pulsarMessageJars.loadedClasses.filter(filter.value)

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            messageClassSelectorUI(
                filter = filter.value,
                onFilterChange = filter::onStateChange,
                filteredClasses = filteredClasses(),
                onSelectedSendClass = ::onSelectedSendClass,
                selectedSendClass = selectedSendClass.selected,
                selectedReceiveClasses = selectedReceiveClasses,
                onSelectedReceiveClass = ::onSelectedReceiveClass,
                listState = listState,
                setUserFeedback = setUserFeedback,
                onChange = onChange,
            )
        }
    }
}
