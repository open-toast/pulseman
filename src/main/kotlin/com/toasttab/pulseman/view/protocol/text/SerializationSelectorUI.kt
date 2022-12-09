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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.CHARACTER_SET
import com.toasttab.pulseman.AppStrings.CLICK_TO_SELECT
import com.toasttab.pulseman.AppStrings.SELECTED_CHARSET
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.CharacterSet

/**
 * TODO
 */
@ExperimentalFoundationApi
@Composable
fun serializationSelectorUI(
    selectedCharacterSet: CharacterSet?,
    onSelectedCharacterSet: (CharacterSet) -> Unit,
    listState: LazyListState
) {
    Column {
        Row {
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
                                text = AnnotatedString(CHARACTER_SET),
                                modifier = Modifier.weight(0.8F).align(Alignment.CenterVertically)
                            )

                            Divider(
                                color = AppTheme.colors.backgroundDark,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                items(CharacterSet.values()) { characterSet ->
                    Card(
                        backgroundColor = AppTheme.colors.backgroundMedium,
                        border = BorderStroke(1.dp, AppTheme.colors.backgroundDark),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = AnnotatedString(characterSet.charSet),
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
                                onClick = { onSelectedCharacterSet(characterSet) }
                            ) {
                                if (selectedCharacterSet == characterSet)
                                    Icon(Icons.Default.RadioButtonChecked, SELECTED_CHARSET)
                                else
                                    Icon(Icons.Default.RadioButtonUnchecked, CLICK_TO_SELECT)
                            }
                        }
                    }
                }
            }
        }
    }
}
