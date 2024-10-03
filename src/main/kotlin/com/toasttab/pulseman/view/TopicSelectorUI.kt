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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.AppStrings.CLICK_TO_SELECT
import com.toasttab.pulseman.AppStrings.FILTER
import com.toasttab.pulseman.AppStrings.NO_TOPICS_FOUND
import com.toasttab.pulseman.AppStrings.PULSAR_URL
import com.toasttab.pulseman.AppStrings.SELECT
import com.toasttab.pulseman.AppStrings.TOPIC
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.entities.ButtonState
import com.toasttab.pulseman.view.ViewUtils.threadedButton
import kotlinx.coroutines.CoroutineScope

/**
 * This view allows the user to query a pulsar setup and get a list of configured topics.
 * If they select one of the topics it will be added to the project.
 *
 * TODO support pulsar authentication for this
 */
@Composable
fun topicSelectorUI(
    scope: CoroutineScope,
    filter: String,
    onFilterChange: (String) -> Unit,
    pulsarUrl: String,
    onPulsarUrlChange: (String) -> Unit,
    onLoadTopics: () -> Unit,
    topicRetrievalState: ButtonState,
    onTopicRetrievalStateChange: (ButtonState) -> Unit,
    filteredTopics: List<String>,
    onSelectSettingsTopic: (String) -> Unit
) {
    val padding = 4.dp
    MaterialTheme(colors = AppTheme.colors.material) {
        CompositionLocalProvider {
            Surface {
                Column(Modifier.fillMaxSize().padding(padding)) {
                    // Filter, Pulsar URL, Load Topics row
                    Row {
                        threadedButton(
                            scope = scope,
                            activeText = AppStrings.LOADING,
                            waitingText = AppStrings.LOAD_TOPICS,
                            buttonState = topicRetrievalState,
                            onButtonStateChange = onTopicRetrievalStateChange
                        ) {
                            onLoadTopics()
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        TextField(
                            label = { Text(FILTER) },
                            value = filter,
                            onValueChange = onFilterChange,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        TextField(
                            label = { Text(PULSAR_URL) },
                            value = pulsarUrl,
                            onValueChange = onPulsarUrlChange,
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredTopics.isEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AnnotatedString(NO_TOPICS_FOUND),
                            modifier = Modifier.weight(1F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        // Topic, Select  list header
                        Card(
                            backgroundColor = AppTheme.colors.backgroundMedium,
                            border = BorderStroke(1.dp, AppTheme.colors.backgroundDark),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = AnnotatedString(TOPIC),
                                    modifier = Modifier.weight(0.8F)
                                        .align(Alignment.CenterVertically)
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
                                    modifier = Modifier.weight(0.1F)
                                        .align(Alignment.CenterVertically)
                                )
                            }
                        }
                        // List values
                        val stateVertical = rememberScrollState(0)
                        Column(modifier = Modifier.verticalScroll(stateVertical)) {
                            filteredTopics.forEach { topic ->
                                Card(
                                    backgroundColor = AppTheme.colors.backgroundMedium,
                                    border = BorderStroke(1.dp, AppTheme.colors.backgroundDark),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Row {
                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = AnnotatedString(topic),
                                            modifier = Modifier.weight(0.8F)
                                                .align(Alignment.CenterVertically),
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
                                            onClick = { onSelectSettingsTopic(topic) }
                                        ) {
                                            Icon(Icons.Default.Clear, CLICK_TO_SELECT)
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
}
