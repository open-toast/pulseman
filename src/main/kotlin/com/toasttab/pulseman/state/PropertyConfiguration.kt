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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

class PropertyConfiguration(
    onChange: () -> Unit,
    initialSettings: TabValuesV3?
) {
    // TODO allow comments in json dialogs
    private val defaultJsonParameters = "{\n}"

    private val textArea =
        RSyntaxTextArea.textArea(
            initialSettings?.propertyMap ?: defaultJsonParameters,
            SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS,
            onChange
        )

    val sp = RTextScrollPane(textArea)

    fun propertyText(): String = textArea.text

    fun propertyMap(setUserFeedback: (String) -> Unit): Map<String, String> {
        val propertiesJsonMap = textArea.text
        if (textArea.text.isNotBlank()) {
            try {
                return mapper.readValue(propertiesJsonMap, mapTypeRef)
            } catch (ex: Exception) {
                setUserFeedback("${AppStrings.FAILED_TO_DESERIALIZE_PROPERTIES}=$propertiesJsonMap. ${AppStrings.EXCEPTION}=$ex")
            }
        }
        return emptyMap()
    }

    companion object {
        private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        private val mapTypeRef = object : TypeReference<Map<String, String>>() {}
    }
}
