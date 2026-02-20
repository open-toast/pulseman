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

import com.toasttab.pulseman.state.protocol.protobuf.ConvertType
import com.toasttab.pulseman.state.protocol.protobuf.ProtobufTabValuesV3
import com.toasttab.pulseman.state.protocol.text.TextTabValuesV3

/**
 * This is deprecated, keeping it for backwards compatibility with old config format, will eventually delete altogether.
 */
@Deprecated(
    "This is an old save format, use ProjectSettingsV3 instead",
    replaceWith = ReplaceWith("ProjectSettingsV3"),
    level = DeprecationLevel.WARNING
)
data class ProjectSettingsV2(
    val configVersion: String,
    @Suppress("DEPRECATION") val tabs: List<TabValuesV2>
) : ProjectSettings {
    override fun toV3(): ProjectSettingsV3 {
        return ProjectSettingsV3(
            configVersion = ProjectSettingsV3.CURRENT_VERSION,
            newJarFormatUsed = false,
            gradleScript = null,
            javaHome = null,
            tabs = tabs.map { tab ->
                TabValuesV3(
                    tabName = tab.tabName,
                    topic = tab.topic,
                    serviceUrl = tab.serviceUrl,
                    selectedAuthClass = tab.selectedAuthClass,
                    authJsonParameters = tab.authJsonParameters,
                    propertyMap = tab.propertyMap,
                    serializationFormat = tab.serializationFormat,
                    protobufSettings = ProtobufTabValuesV3(
                        code = tab.protobufSettings?.code,
                        selectedClass = tab.protobufSettings?.selectedClassSend,
                        convertValue = null,
                        convertType = ConvertType.BASE64
                    ),
                    textSettings = TextTabValuesV3(
                        text = tab.textSettings?.text,
                        selectedEncoding = tab.textSettings?.selectedSendEncoding
                    ),
                    pulsarAdminURL = null,
                    tabExtension = null,
                    gradleScript = null,
                    propertyFilters = emptyList()
                )
            }
        )
    }
}
