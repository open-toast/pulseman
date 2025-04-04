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
import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.scripting.GradleScripting
import com.toasttab.pulseman.thirdparty.rsyntaxtextarea.RSyntaxTextArea
import com.toasttab.pulseman.view.gradleManagementUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane

class GradleManagement(
    private val setUserFeedback: (String) -> Unit,
    private val commonJarManager: JarManager<ClassInfo>?,
    pulsarJarManagers: List<JarManager<out ClassInfo>>,
    onChange: () -> Unit,
    taskPrefix: String,
    gradleScript: String? = null,
    fileManagement: FileManagement
) {
    private val taskName = "${taskPrefix}_task"
    private val generateState = mutableStateOf(ButtonState.WAITING)
    private val compileState = mutableStateOf(ButtonState.WAITING)
    private val filterPulsarJars = mutableStateOf(true)

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val textArea =
        RSyntaxTextArea.textArea(
            gradleScript ?: GradleScripting.gradleTemplate,
            SyntaxConstants.SYNTAX_STYLE_KOTLIN,
            onChange
        )

    private val gradleRunner = GradleRunner(
        setUserFeedback = setUserFeedback,
        commonJarManager = commonJarManager,
        pulsarJarManagers = pulsarJarManagers,
        onChange = onChange,
        taskName = taskName,
        filterPulsarJars = filterPulsarJars,
        fileManagement = fileManagement
    )

    private fun generateGradleTemplate() {
        textArea.text = GradleScripting.gradleTemplate
    }

    private fun runGradleTask() {
        compileState.value = ButtonState.ACTIVE
        runBlocking {
            scope.run {
                try {
                    withTimeout(GRADLE_TASK_TIMEOUT) {
                        gradleRunner.runGradleTask(gradleScript = textArea.text)
                    }
                } catch (ex: Exception) {
                    setUserFeedback("Gradle task failed: ${ex.message}")
                }
                compileState.value = ButtonState.WAITING
            }
        }
    }

    fun cleanUp() {
        scope.cancel(CANCEL_SCOPE_LOG)
    }

    private val sp = RTextScrollPane(textArea)

    fun currentGradleScript(): String = textArea.text

    fun loadGradleScript(gradleScript: String) {
        textArea.text = gradleScript
    }

    fun getUI(): @Composable () -> Unit {
        return {
            gradleManagementUI(
                scope = scope,
                generateState = generateState.value,
                onGenerateStateChange = generateState::onStateChange,
                gradleRunState = compileState.value,
                onGradleRunStateChange = compileState::onStateChange,
                generateGradleTemplate = ::generateGradleTemplate,
                isFilterPulsarSelected = filterPulsarJars.value,
                onFilterPulsarSelected = filterPulsarJars::onStateChange,
                showFilterToggle = commonJarManager != null,
                runGradleTask = ::runGradleTask,
                scrollPane = sp
            )
        }
    }

    companion object {
        private const val CANCEL_SCOPE_LOG = "Shutting down GradleManagement"
        private const val GRADLE_TASK_TIMEOUT = 120000L
    }
}
