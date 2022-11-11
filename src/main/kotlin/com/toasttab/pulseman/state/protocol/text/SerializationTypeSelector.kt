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

package com.toasttab.pulseman.state.protocol.text

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.entities.CharacterSet
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import com.toasttab.pulseman.pulsar.handlers.text.TextHandler
import com.toasttab.pulseman.view.serializationSelectorUI

class SerializationTypeSelector(
    val selectedEncoding: SingleSelection<PulsarMessage> = SingleSelection(),
    private val listState: LazyListState = LazyListState(),
    val setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    initialSettings: TabValuesV3?
) {
    init {
        selectedEncoding.selected = initialSettings?.textSettings?.selectedEncoding?.let { charset ->
            TextHandler(characterSet = CharacterSet.fromCharSet(charset))
        } ?: TextHandler(characterSet = CharacterSet.UTF_8)
    }

    val selectedCharacterSet: CharacterSet?
        get() = (selectedEncoding.selected as TextHandler?)?.characterSet

    private fun onSelectedEncoding(newValue: CharacterSet) {
        selectedEncoding.selected = TextHandler(characterSet = newValue)
        setUserFeedback("${AppStrings.SELECTED_SEND_CHARSET} ${newValue.charSet}")
        onChange()
    }

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            serializationSelectorUI(
                listState = listState,
                selectedCharacterSet = (selectedEncoding.selected as TextHandler?)?.characterSet,
                onSelectedCharacterSet = ::onSelectedEncoding
            )
        }
    }
}
