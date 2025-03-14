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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toasttab.pulseman.AppStrings

/**
 * This view allows the user to select from a list of options
 */
@Composable
fun dropdownSelectorUI(
    expanded: Boolean,
    currentlySelected: String?,
    noOptionSelected: String = "",
    options: List<String>,
    onChangeExpanded: () -> Unit,
    onSelectedOption: (String) -> Unit
) {
    Box {
        IconButton(
            onClick = { onChangeExpanded() }
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Transparent)
                    .border(width = 0.8.dp, color = Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            ) {
                Row(modifier = Modifier.background(Color.Transparent).padding(8.dp, 8.dp)) {
                    if (currentlySelected != null) {
                        Text(currentlySelected)
                    } else {
                        Text(noOptionSelected)
                    }
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = AppStrings.CHOOSE_OPTION)
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onChangeExpanded() }
        ) {
            options.forEachIndexed { index, format ->
                DropdownMenuItem(
                    onClick = {
                        onSelectedOption(format)
                        onChangeExpanded()
                    }
                ) {
                    Text(
                        text = format,
                        modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (index < options.size - 1) {
                    Divider()
                }
            }
        }
    }
}
