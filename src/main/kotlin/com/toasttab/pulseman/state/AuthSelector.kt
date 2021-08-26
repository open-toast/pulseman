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
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.entities.TabValues
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.Theme
import org.fife.ui.rtextarea.RTextScrollPane

class AuthSelector(
    private val authJars: JarManager<PulsarAuthHandler>,
    val selectedAuthClass: SingleSelection<PulsarAuthHandler> = SingleSelection(),
    val filter: MutableState<String> = mutableStateOf(""),
    private val setUserFeedback: (String) -> Unit,
    private val onChange: () -> Unit,
    initialSettings: TabValues?
) {
    private val defaultJsonParameters = "// TODO add your credential values"

    private val textArea =
        RSyntaxTextArea.textArea(
            initialSettings?.authJsonParameters ?: defaultJsonParameters,
            SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS,
            onChange
        )

    init {
        initialSettings?.selectedAuthClass?.let { savedSelection ->
            selectedAuthClass.selected = authJars.loadedClasses.getClass(savedSelection)
        }

        Theme.load(
            javaClass.getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"
            )
        ).apply { apply(textArea) }
    }

    val sp = RTextScrollPane(textArea)

    fun onFilterChange(newValue: String) {
        filter.value = newValue
    }

    fun onSelectedAuthClass(newValue: PulsarAuthHandler) {
        selectedAuthClass.selected =
            if (selectedAuthClass.selected == newValue)
                null
            else newValue
        setUserFeedback("Selected ${newValue.cls.name}")
        onChange()
    }

    fun filteredClasses() = authJars.loadedClasses.filter(filter.value)

    fun authJsonParameters(): String = textArea.text
}
