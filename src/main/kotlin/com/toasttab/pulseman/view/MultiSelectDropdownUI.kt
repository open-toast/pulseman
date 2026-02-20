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
import androidx.compose.material.Checkbox
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
 * This view allows the user to select multiple options from a dropdown list
 */
@Composable
fun multiSelectDropdownUI(
    label: String,
    expanded: Boolean,
    onChangeExpanded: () -> Unit,
    options: Map<String, String>,
    selectedValues: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
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
                    Text(
                        text = if (selectedValues.isEmpty()) {
                            label
                        } else {
                            "$label (${selectedValues.size})"
                        }
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = AppStrings.CHOOSE_OPTION)
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onChangeExpanded() }
        ) {
            options.keys.forEachIndexed { index, key ->
                DropdownMenuItem(
                    onClick = {
                        val newSelection = if (selectedValues.contains(key)) {
                            selectedValues - key
                        } else {
                            selectedValues + key
                        }
                        onSelectionChanged(newSelection)
                    }
                ) {
                    Row(
                        modifier = Modifier.weight(1F),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedValues.contains(key),
                            onCheckedChange = null
                        )
                        Text(
                            text = key,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (index < options.size - 1) {
                    Divider()
                }
            }
        }
    }
}
