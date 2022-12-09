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

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.AppStrings.FAILED_TO_CONVERT_BYTES
import com.toasttab.pulseman.AppStrings.PROTO_CLASS_NOT_SELECTED
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.state.DropdownSelector
import com.toasttab.pulseman.state.onStateChange
import com.toasttab.pulseman.view.protocol.protobuf.convertProtobufUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class ConvertProtobufMessage(
    private val setUserFeedback: (String) -> Unit,
    private val selectedClass: SingleSelection<PulsarMessageClassInfo>,
    convertValue: String?,
    convertType: ConvertType?,
    private val onChange: () -> Unit
) {
    private val convertState = mutableStateOf(ButtonState.WAITING)
    private val convertedMessage = mutableStateOf("")
    private val convertValueState: MutableState<String> = mutableStateOf(convertValue ?: "")
    private val convertTypeState: MutableState<ConvertType> = mutableStateOf(convertType ?: ConvertType.BASE64)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val convertValueScrollState = ScrollState(0)
    private val convertedScrollState = ScrollState(0)

    fun close() {
        scope.cancel(CANCEL_SCOPE_LOG)
    }

    private fun convert() {
        try {
            val bytes = convertTypeState.value.toBytes(convertValueState.value, setUserFeedback)
            selectedClass.selected?.let { proto ->
                convertedMessage.value = proto.prettyPrint(proto.deserialize(bytes))
            } ?: run {
                setUserFeedback(PROTO_CLASS_NOT_SELECTED)
            }
        } catch (ex: Exception) {
            setUserFeedback("$FAILED_TO_CONVERT_BYTES ex:$ex")
        }
    }

    private val convertTypeSelector = DropdownSelector(
        options = ConvertType.values().map { it.name },
        onSelected = {
            convertTypeState.value = ConvertType.valueOf(it)
            setUserFeedback("${AppStrings.SELECTED} $it ${AppStrings.SERIALIZATION_FORMAT}")
            onChange()
        }
    )

    fun currentConvertValue(): String = convertValueState.value

    fun currentConvertType(): ConvertType = convertTypeState.value

    fun getUI(): @Composable () -> Unit {
        return {
            convertProtobufUI(
                scope = scope,
                convertState = convertState.value,
                onConvertStateChange = convertState::onStateChange,
                convertValue = convertValueState.value,
                onConvertValueChange = { convertValueState.onStateChange(it, onChange) },
                convertTypeSelectorUI = convertTypeSelector.getUI(
                    currentlySelected = convertTypeState.value.name
                ),
                convert = ::convert,
                convertedMessage = convertedMessage.value,
                convertValueScrollState = convertValueScrollState,
                convertedScrollState = convertedScrollState
            )
        }
    }

    companion object {
        private const val CANCEL_SCOPE_LOG = "Shutting down ConvertProtobufMessage"
    }
}
