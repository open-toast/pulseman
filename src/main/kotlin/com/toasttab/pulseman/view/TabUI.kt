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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings.TAB_NAME
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.view.ViewUtils.styledTextField

/**
 * This holds all the views and settings form an individual tab.
 */
@Composable
fun tabUI(
    tabName: String,
    onTabNameChange: (String) -> Unit,
    userFeedbackUI: @Composable () -> Unit,
    pulsarSettingsUI: @Composable () -> Unit,
    protocolUI: @Composable () -> Unit,
    serializationFormatSelectorUI: @Composable () -> Unit
) {
    val padding = 4.dp
    Surface(Modifier.fillMaxSize(), color = AppTheme.colors.backgroundDark) {
        Column {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(color = AppTheme.colors.backgroundLight)
                    .border(2.dp, AppTheme.colors.backgroundMedium)
                    .padding(2.dp)
            ) {
                Column {
                    Row {
                        styledTextField(
                            label = TAB_NAME,
                            field = tabName,
                            modifier = Modifier.padding(padding).fillMaxWidth(0.25F),
                            onValueChange = onTabNameChange
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.align(alignment = Alignment.CenterVertically)) {
                            serializationFormatSelectorUI()
                        }
                    }
                    pulsarSettingsUI()
                }
            }
            Box(Modifier.weight(0.85F).padding(4.dp)) {
                protocolUI()
            }
            Box(Modifier.weight(0.15F)) {
                userFeedbackUI()
            }
        }
    }
}
