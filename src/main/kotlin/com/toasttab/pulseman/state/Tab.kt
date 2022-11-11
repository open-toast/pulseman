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

package com.toasttab.pulseman.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.toasttab.pulseman.entities.TabValuesV3

data class Tab(
    val tabName: String,
    val close: () -> Unit,
    val unsavedChanges: Boolean,
    val initialSettings: TabValuesV3?,
    val isActive: Boolean,
    val onFocusedUpdate: (Boolean) -> Unit,
    val activate: () -> Unit,
    val image: ImageVector,
    val onEnterIconUnsavedChanges: () -> Unit,
    val onExitIconUnsavedChanges: () -> Unit,
    val onEnterIcon: () -> Unit,
    val onExitIcon: () -> Unit,
    val drawBackground: Boolean,
    val focused: Boolean,
    val ui: @Composable () -> Unit
)
