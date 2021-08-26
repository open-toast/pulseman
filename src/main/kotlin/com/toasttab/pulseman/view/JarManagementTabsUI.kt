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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.state.JarManagementTabs

/**
 * Groups all the JarManagement views in the project into a tabbed popup
 */
@ExperimentalFoundationApi
@Composable
fun jarManagementTabsUI(state: JarManagementTabs) {
    MaterialTheme(colors = AppTheme.colors.material) {
        DesktopTheme {
            Surface {
                Column {
                    Box(Modifier.padding(4.dp)) {
                        Column {
                            selectJarViewUI(state)
                            Box(
                                Modifier
                                    .background(color = AppTheme.colors.backgroundLight)
                                    .padding(2.dp)
                            ) {
                                jarManagementUI(state.currentView.value.second)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun selectJarViewUI(state: JarManagementTabs) {
    Row {
        state.jarManagers.forEach {
            Surface(color = if (state.currentView.value == it) AppTheme.colors.backgroundLight else AppTheme.colors.backgroundMedium) {
                Row(
                    modifier = Modifier.clickable(onClick = { state.currentView.value = it }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        it.first,
                        color = Color.White,
                        fontSize = 18.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}
