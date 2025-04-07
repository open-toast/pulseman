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

package com.toasttab.pulseman.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.AppStrings.GENERATE
import com.toasttab.pulseman.AppStrings.GENERATING
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import kotlinx.coroutines.CoroutineScope
import org.fife.ui.rtextarea.RTextScrollPane
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * This view allows the user to
 * - Generate a gradle script template
 * - Edit and compile gradle scripts to download dependencies
 */
@Composable
fun gradleManagementUI(
    scope: CoroutineScope,
    generateState: ButtonState,
    onGenerateStateChange: (ButtonState) -> Unit,
    gradleRunState: ButtonState,
    onGradleRunStateChange: (ButtonState) -> Unit,
    generateGradleTemplate: () -> Unit,
    isFilterPulsarSelected: Boolean,
    onFilterPulsarSelected: (Boolean) -> Unit,
    showFilterToggle: Boolean,
    runGradleTask: () -> Unit,
    scrollPane: RTextScrollPane
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            threadedButton(
                scope = scope,
                activeText = GENERATING,
                waitingText = GENERATE,
                buttonState = generateState,
                onButtonStateChange = onGenerateStateChange
            ) {
                generateGradleTemplate()
            }

            threadedButton(
                scope = scope,
                activeText = AppStrings.RUNNING,
                waitingText = AppStrings.RUN,
                buttonState = gradleRunState,
                onButtonStateChange = onGradleRunStateChange
            ) {
                runGradleTask()
            }

            if (showFilterToggle) {
                Switch(
                    checked = isFilterPulsarSelected,
                    onCheckedChange = onFilterPulsarSelected,
                    colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.colors.backgroundDark)
                )
                Text(text = AppStrings.FILTER_PULSAR)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            SwingPanel(
                background = Color.White,
                modifier = Modifier.fillMaxSize(),
                factory = {
                    JPanel().apply {
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        add(scrollPane)
                    }
                }
            )
        }
    }
}
