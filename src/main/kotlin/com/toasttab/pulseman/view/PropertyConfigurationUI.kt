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

import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.SwingPanel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.state.PropertyConfiguration
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * This view is a simple properties configuration view.
 * It allows the user to define a hash map of key value pairs of property settings in Json.
 */
@ExperimentalFoundationApi
@Composable
fun propertyConfigurationUI(state: PropertyConfiguration) {
    MaterialTheme(colors = AppTheme.colors.material) {
        DesktopTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column {
                    Box(
                        modifier = Modifier.weight(0.5f).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SwingPanel(
                            background = Color.White,
                            modifier = Modifier.fillMaxSize(),
                            factory = {
                                JPanel().apply {
                                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                                    add(state.sp)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
