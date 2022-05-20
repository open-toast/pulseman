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
import com.toasttab.pulseman.entities.TabValuesV2
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import com.toasttab.pulseman.pulsar.handlers.text.TextHandler
import com.toasttab.pulseman.view.serializationSelectorUI

class SerializationTypeSelector(
    val selectedSendEncoding: SingleSelection<PulsarMessage> = SingleSelection(),
    val selectedReceiveEncoding: SingleSelection<PulsarMessage> = SingleSelection(),
    private val listState: LazyListState = LazyListState(),
    val setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    initialSettings: TabValuesV2?
) {
    init {
        selectedSendEncoding.selected = initialSettings?.textSettings?.selectedSendEncoding?.let { charset ->
            TextHandler(characterSet = CharacterSet.fromCharSet(charset))
        } ?: TextHandler(characterSet = CharacterSet.UTF_8)

        selectedReceiveEncoding.selected = initialSettings?.textSettings?.selectedReceiveEncoding?.let { charset ->
            TextHandler(characterSet = CharacterSet.fromCharSet(charset))
        } ?: TextHandler(characterSet = CharacterSet.UTF_8)
    }

    val selectedSendCharacterSet: CharacterSet?
        get() = (selectedSendEncoding.selected as TextHandler?)?.characterSet

    val selectedReceiveCharacterSet: CharacterSet?
        get() = (selectedReceiveEncoding.selected as TextHandler?)?.characterSet

    private fun onSelectedSendEncoding(newValue: CharacterSet) {
        selectedSendEncoding.selected = TextHandler(characterSet = newValue)
        setUserFeedback("${AppStrings.SELECTED_SEND_CHARSET} ${newValue.charSet}")
        onChange()
    }

    private fun onSelectedReceiveEncoding(newValue: CharacterSet) {
        selectedReceiveEncoding.selected = TextHandler(characterSet = newValue)
        setUserFeedback("${AppStrings.SELECTED_RECEIVE_CHARSET} ${newValue.charSet}")
        onChange()
    }

    @ExperimentalFoundationApi
    fun getUI(): @Composable () -> Unit {
        return {
            serializationSelectorUI(
                listState = listState,
                selectedSendCharacterSet = (selectedSendEncoding.selected as TextHandler?)?.characterSet,
                selectedReceiveCharacterSet = (selectedReceiveEncoding.selected as TextHandler?)?.characterSet,
                onSelectedSendCharacterSet = ::onSelectedSendEncoding,
                onSelectedReceiveCharacterSet = ::onSelectedReceiveEncoding
            )
        }
    }
}
