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
import androidx.compose.runtime.Composable
import com.toasttab.pulseman.AppState
import com.toasttab.pulseman.entities.SerializationFormat
import com.toasttab.pulseman.entities.TabValuesV2
import com.toasttab.pulseman.state.protocol.protobuf.ProtobufState
import com.toasttab.pulseman.state.protocol.text.TextState

/**
 * This class store the state of all the supported serialization formats and returns
 * the ui for the currently selected format
 */
class SerializationState(
    appState: AppState,
    initialSettings: TabValuesV2? = null,
    pulsarSettings: PulsarSettings,
    setUserFeedback: (String) -> Unit,
    onChange: () -> Unit
) {
    fun cleanUp() {
        protobufState.cleanUp()
        textState.cleanUp()
    }

    val protobufState = ProtobufState(
        appState = appState,
        initialSettings = initialSettings,
        pulsarSettings = pulsarSettings,
        setUserFeedback = setUserFeedback,
        onChange = onChange
    )

    val textState = TextState(
        appState = appState,
        initialSettings = initialSettings,
        pulsarSettings = pulsarSettings,
        setUserFeedback = setUserFeedback,
        onChange = onChange
    )

    @ExperimentalFoundationApi
    fun getUI(serializationFormat: SerializationFormat): @Composable () -> Unit {
        return when (serializationFormat) {
            SerializationFormat.PROTOBUF -> {
                protobufState.getUI()
            }
            SerializationFormat.TEXT -> {
                textState.getUI()
            }
        }
    }
}
