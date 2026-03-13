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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.AppStrings.DEFAULT_BODY_FILTER_SCRIPT
import com.toasttab.pulseman.entities.ActiveBodyFilter
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.scripting.KotlinScripting
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import com.toasttab.pulseman.view.bodyFilterUI
import kotlinx.coroutines.CoroutineScope
import org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_KOTLIN
import org.fife.ui.rtextarea.RTextScrollPane

class BodyFilter(
    initialScript: String? = null,
    private val setUserFeedback: (String) -> Unit,
    onChange: () -> Unit
) {
    private val enabled = mutableStateOf(false)
    private val generateState: MutableState<ButtonState> = mutableStateOf(ButtonState.WAITING)
    private val compileState: MutableState<ButtonState> = mutableStateOf(ButtonState.WAITING)
    private val _activeFilter: MutableState<ActiveBodyFilter?> = mutableStateOf(null)

    val activeFilter: ActiveBodyFilter?
        get() = if (enabled.value) _activeFilter.value else null

    private val textArea = RSyntaxTextArea.textArea(initialScript ?: DEFAULT_BODY_FILTER_SCRIPT, SYNTAX_STYLE_KOTLIN, onChange)
    private val sp = RTextScrollPane(textArea)

    fun generateTemplate(text: String) {
        textArea.text = text
    }

    fun compilePredicate(jarLoader: JarLoader) {
        val predicate = KotlinScripting.compilePredicate(textArea.text, jarLoader, setUserFeedback)
        _activeFilter.value = predicate?.let { ActiveBodyFilter(it) }
    }

    fun currentScript(): String = textArea.text

    fun getUI(scope: CoroutineScope, onGenerate: () -> Unit, onCompile: () -> Unit): @Composable () -> Unit = {
        bodyFilterUI(
            scope = scope,
            enabled = enabled.value,
            onEnabledChange = { enabled.value = it },
            generateState = generateState.value,
            onGenerateStateChange = generateState::onStateChange,
            onGenerate = onGenerate,
            compileState = compileState.value,
            onCompileStateChange = compileState::onStateChange,
            onCompile = onCompile,
            scrollPane = sp
        )
    }
}
