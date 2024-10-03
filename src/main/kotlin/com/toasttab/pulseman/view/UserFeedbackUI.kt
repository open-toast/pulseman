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

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.CLEAR_LOG
import com.toasttab.pulseman.AppTheme

/**
 * This views shows a scrollable history of events in the current tab.
 * It is used by passing the set function to all views that need it.
 */
@Composable
fun userFeedbackUI(
    userFeedback: List<String>,
    onUserFeedbackClear: () -> Unit
) {
    SelectionContainer {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color = AppTheme.colors.backgroundLight)
                .border(width = 2.dp, color = AppTheme.colors.backgroundMedium)
                .padding(all = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(state = ScrollState(0), reverseScrolling = true)
                    .fillMaxWidth(fraction = 0.95f)
            ) {
                userFeedback.forEach {
                    Text(text = it, softWrap = true)
                }
            }
            Column(
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            ) {
                IconButton(
                    onClick = { onUserFeedbackClear() },
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = CLEAR_LOG)
                }
            }
        }
    }
}
