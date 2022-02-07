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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.AUTH_PARAMETERS
import com.toasttab.pulseman.AppStrings.CLICK_TO_SELECT
import com.toasttab.pulseman.AppStrings.FILTER
import com.toasttab.pulseman.AppStrings.NAME
import com.toasttab.pulseman.AppStrings.NO_VALID_CLASSES_LOADED
import com.toasttab.pulseman.AppStrings.SELECT
import com.toasttab.pulseman.AppStrings.SELECTED_CLASS
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import org.fife.ui.rtextarea.RTextScrollPane
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * This view is set up to allow the user to
 * - View any Pulsar Authentication classes
 * - Filter those classes
 * - Select a class to use for Authentication
 * - Define any auth setting in a string format
 */
@ExperimentalFoundationApi
@Composable
fun authSelectorUI(
    filter: String,
    onFilterChange: (String) -> Unit,
    filteredClasses: List<PulsarAuthHandler>,
    selectedAuthClass: PulsarAuthHandler?,
    onSelectedAuthClass: (PulsarAuthHandler) -> Unit,
    scrollPane: RTextScrollPane
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            TextField(
                label = { Text(FILTER) },
                value = filter,
                onValueChange = onFilterChange,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.5f).padding(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.weight(0.3F)) {
                val listState = rememberLazyListState()
                if (filteredClasses.isEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AnnotatedString(NO_VALID_CLASSES_LOADED),
                        modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    LazyColumn(state = listState) {
                        stickyHeader {
                            Card(
                                backgroundColor = AppTheme.colors.backgroundMedium,
                                border = BorderStroke(1.dp, AppTheme.colors.backgroundDark),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = AnnotatedString(NAME),
                                        modifier = Modifier.weight(0.8F).align(Alignment.CenterVertically)
                                    )

                                    Divider(
                                        color = AppTheme.colors.backgroundDark,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = AnnotatedString(SELECT),
                                        modifier = Modifier.weight(0.1F).align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }

                        items(filteredClasses.size) { i ->
                            val classInfo = filteredClasses[i]
                            Card(
                                backgroundColor = AppTheme.colors.backgroundMedium,
                                border = BorderStroke(1.dp, AppTheme.colors.backgroundDark),
                                modifier = Modifier.height(40.dp)
                            ) {
                                Row {
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = AnnotatedString(classInfo.cls.name),
                                        modifier = Modifier.weight(0.8F).align(Alignment.CenterVertically),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Divider(
                                        color = AppTheme.colors.backgroundDark,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        modifier = Modifier.weight(0.1F),
                                        onClick = {
                                            onSelectedAuthClass(classInfo)
                                        }
                                    ) {
                                        if (selectedAuthClass === classInfo)
                                            Icon(Icons.Default.CheckCircle, SELECTED_CLASS)
                                        else
                                            Icon(Icons.Default.Clear, CLICK_TO_SELECT)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = AnnotatedString(AUTH_PARAMETERS),
                modifier = Modifier.padding(4.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

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
                            add(scrollPane)
                        }
                    }
                )
            }
        }
    }
}
