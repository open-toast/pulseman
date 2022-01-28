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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.ADD_JAR
import com.toasttab.pulseman.AppStrings.DELETE_JAR
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.files.FileManagement.addFileDialog
import java.io.File

/**
 * This view allows a user to add or remove a jar to a JarManager and store that jar in the project.
 */
@Composable
fun jarManagementUI(
    loadedJars: List<File>,
    jarFolder: File,
    onRemoveJar: (File) -> Unit,
    onAddJar: (File) -> Unit,
) {
    Surface {
        Box {
            val stateVertical = rememberScrollState(0)
            Column(modifier = Modifier.verticalScroll(stateVertical)) {
                loadedJars.forEach { jar ->
                    Card(
                        backgroundColor = AppTheme.colors.backgroundMedium,
                        border = BorderStroke(1.dp, AppTheme.colors.backgroundDark)
                    ) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = AnnotatedString(jar.name),
                                modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { onRemoveJar(jar) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = DELETE_JAR)
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
                Card(
                    backgroundColor = AppTheme.colors.backgroundMedium,
                    border = BorderStroke(1.dp, AppTheme.colors.backgroundDark)
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = AnnotatedString(ADD_JAR),
                            modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                addFileDialog(jarFolder) {
                                    onAddJar(it)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = ADD_JAR)
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical)
            )
        }
    }
}
