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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.CLICK_TO_SELECT
import com.toasttab.pulseman.AppStrings.FILTER
import com.toasttab.pulseman.AppStrings.NAME
import com.toasttab.pulseman.AppStrings.NO_VALID_CLASSES_LOADED
import com.toasttab.pulseman.AppStrings.SELECTED_CLASS
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo

/**
 * This view presents a list of all the pulsar message classes loaded to the project.
 * The user can filter this list and select classes to serialize and deserialize with.
 * You can select 1 class to serialize messages and multiple to deserialize
 */
@ExperimentalFoundationApi
@Composable
fun messageClassSelectorUI(
    filter: String,
    onFilterChange: (String) -> Unit,
    filteredClasses: List<PulsarMessageClassInfo>,
    selectedClass: PulsarMessageClassInfo?,
    onSelectedClass: (PulsarMessageClassInfo) -> Unit,
    listState: LazyListState
) {
    Column {
        TextField(
            label = { Text(FILTER) },
            value = filter,
            onValueChange = onFilterChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.5f).padding(4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row {
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
                            }
                        }
                    }

                    items(filteredClasses) { classInfo ->
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
                                    onClick = { onSelectedClass(classInfo) }
                                ) {
                                    if (selectedClass?.cls?.name == classInfo.cls.name) {
                                        Icon(Icons.Default.RadioButtonChecked, SELECTED_CLASS)
                                    } else {
                                        Icon(Icons.Default.RadioButtonUnchecked, CLICK_TO_SELECT)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
