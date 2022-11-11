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

package com.toasttab.pulseman.entities

import com.toasttab.pulseman.state.protocol.protobuf.ProtobufTabValuesV3
import com.toasttab.pulseman.state.protocol.text.TextTabValuesV3

/**
 * Stores the settings for each tab, will be serialized to a json string and saved in the project zip.
 */
data class TabValuesV3(
    val tabName: String?,
    val topic: String?,
    val serviceUrl: String?,
    val selectedAuthClass: String?,
    val authJsonParameters: String?,
    val propertyMap: String?,
    val serializationFormat: SerializationFormat?,
    val protobufSettings: ProtobufTabValuesV3?,
    val textSettings: TextTabValuesV3?
)
