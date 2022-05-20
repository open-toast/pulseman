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

import com.toasttab.pulseman.state.protocol.protobuf.ProtobufTabValues
import com.toasttab.pulseman.state.protocol.text.TextTabValues

/**
 * This is deprecated, keeping it for backwards compatibility with old config format, will eventually delete altogether.
 */
@Deprecated(
    "This is an old save format, use ProjectSettingsV2 instead",
    replaceWith = ReplaceWith("ProjectSettingsV2"),
    level = DeprecationLevel.WARNING
)
data class ProjectSettings(@Suppress("DEPRECATION") val tabs: List<TabValues>) {
    fun toV2(): List<TabValuesV2> {
        return tabs.map { tab ->
            TabValuesV2(
                tabName = tab.tabName,
                topic = tab.topic,
                serviceUrl = tab.serviceUrl,
                selectedAuthClass = tab.selectedAuthClass,
                authJsonParameters = tab.authJsonParameters,
                propertyMap = tab.propertyMap,
                serializationFormat = SerializationFormat.PROTOBUF,
                protobufSettings = ProtobufTabValues(
                    code = tab.code,
                    selectedClassSend = tab.selectedClassSend,
                    selectedClassReceive = tab.selectedClassReceive
                ),
                textSettings = TextTabValues(
                    text = null,
                    selectedSendEncoding = null,
                    selectedReceiveEncoding = null
                )
            )
        }
    }
}
