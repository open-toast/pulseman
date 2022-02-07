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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.state.JarManagement
import java.io.File

/**
 * Groups all the JarManagement views in the project into a tabbed popup
 */
@Composable
fun jarManagementTabsUI(
    loadedJars: List<File>,
    jarFolder: File,
    onRemoveJar: (File) -> Unit,
    onAddJar: (File) -> Unit,
    jarManagers: List<Pair<String, JarManagement<out ClassInfo>>>,
    currentView: Pair<String, JarManagement<out ClassInfo>>,
    setCurrentView: (Pair<String, JarManagement<out ClassInfo>>) -> Unit
) {
    Surface {
        Column {
            Box(Modifier.padding(4.dp)) {
                Column {
                    selectJarViewUI(
                        jarManagers = jarManagers,
                        currentView = currentView,
                        setCurrentView = setCurrentView
                    )
                    Box(
                        Modifier
                            .background(color = AppTheme.colors.backgroundLight)
                            .padding(2.dp)
                    ) {
                        jarManagementUI(
                            loadedJars,
                            jarFolder,
                            onRemoveJar,
                            onAddJar
                        )
                    }
                }
            }
        }
    }
}
