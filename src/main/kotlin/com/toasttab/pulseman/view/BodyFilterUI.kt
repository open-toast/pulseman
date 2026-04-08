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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.AppStrings.COMPILE
import com.toasttab.pulseman.AppStrings.COMPILING_PREDICATE
import com.toasttab.pulseman.AppStrings.GENERATE
import com.toasttab.pulseman.AppStrings.GENERATING
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import kotlinx.coroutines.CoroutineScope
import org.fife.ui.rtextarea.RTextScrollPane
import javax.swing.BoxLayout
import javax.swing.JPanel

@Composable
fun bodyFilterUI(
    scope: CoroutineScope,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    generateState: ButtonState,
    onGenerateStateChange: (ButtonState) -> Unit,
    onGenerate: () -> Unit,
    compileState: ButtonState,
    onCompileStateChange: (ButtonState) -> Unit,
    onCompile: () -> Unit,
    scrollPane: RTextScrollPane
) {
    MaterialTheme(colors = AppTheme.colors.material) {
        CompositionLocalProvider {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = AppStrings.ENABLED)
                        Switch(
                            checked = enabled,
                            onCheckedChange = onEnabledChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = AppTheme.colors.backgroundDark)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        threadedButton(
                            scope = scope,
                            activeText = GENERATING,
                            waitingText = GENERATE,
                            buttonState = generateState,
                            onButtonStateChange = onGenerateStateChange
                        ) {
                            onGenerate()
                        }
                        threadedButton(
                            scope = scope,
                            activeText = COMPILING_PREDICATE,
                            waitingText = COMPILE,
                            buttonState = compileState,
                            onButtonStateChange = onCompileStateChange
                        ) {
                            onCompile()
                        }
                    }
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
    }
}
