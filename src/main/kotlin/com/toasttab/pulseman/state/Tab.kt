package com.toasttab.pulseman.state

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.toasttab.pulseman.entities.TabValues

data class Tab(
    val tabName: String,
    val close: () -> Unit,
    val unsavedChanges: Boolean,
    val initialSettings: TabValues?,
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
