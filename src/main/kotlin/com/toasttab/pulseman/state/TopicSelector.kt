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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.pulsar.PulsarConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TopicSelector(
    private val settingsTopic: MutableState<String>,
    private val showDiscover: MutableState<Boolean> = mutableStateOf(false),
    val setUserFeedback: (String) -> Unit,
) {
    private val pulsarConfig = PulsarConfig(setUserFeedback)
    private val topics: SnapshotStateList<String> = mutableStateListOf()

    val filter: MutableState<String> = mutableStateOf("")
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val topicRetrievalState = mutableStateOf(ButtonState.WAITING)
    val pulsarUrl = mutableStateOf("http://localhost:8079")

    fun onFilterChange(newValue: String) {
        filter.value = newValue
    }

    fun filteredTopics() = topics.filter { it.contains(filter.value) }.sortedBy { it }

    fun onLoadTopics() {
        val newTopicList = pulsarConfig.getTopics(pulsarUrl.value).toMutableList()
        topics.removeAll { true }
        topics.addAll(newTopicList)
    }

    fun onPulsarUrlChange(newValue: String) {
        pulsarUrl.value = newValue
    }

    fun onSelectSettingsTopic(selectedTopic: String) {
        settingsTopic.value = selectedTopic
        setUserFeedback("Selected $selectedTopic")
        showDiscover.value = false
    }
}
