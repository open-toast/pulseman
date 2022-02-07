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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.CLEAR_LOG
import com.toasttab.pulseman.AppTheme

/**
 * This views shows a scrollable history of events in the current tab.
 * It is used by passing the setUserFeedback function to all views that need it.
 *
 * TODO get selectable text working in this view
 */
@Composable
fun userFeedbackUI(
    userFeedback: String,
    onUserFeedbackClear: () -> Unit
) {
    Row {
        TextField(
            label = { },
            value = TextFieldValue(
                text = userFeedback,
                // Force the userfeedback to always show the end of the text
                selection = TextRange(userFeedback.length)
            ),
            readOnly = true,
            onValueChange = {},
            trailingIcon = {
                IconButton(
                    onClick = { onUserFeedbackClear() }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = CLEAR_LOG)
                }
            },
            modifier = Modifier
                .padding(1.dp)
                .fillMaxSize()
                .background(color = AppTheme.colors.backgroundLight)
                .border(2.dp, AppTheme.colors.backgroundMedium)
                .padding(2.dp)
        )
    }
}
