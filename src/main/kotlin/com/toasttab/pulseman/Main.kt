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

package com.toasttab.pulseman

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.toasttab.pulseman.AppResources.PULSE_LOGO
import com.toasttab.pulseman.AppStrings.COPY_CURRENT_TAB
import com.toasttab.pulseman.AppStrings.FAILED_TO_LOAD_PROJECT
import com.toasttab.pulseman.AppStrings.FILE
import com.toasttab.pulseman.AppStrings.LOAD_PROJECT
import com.toasttab.pulseman.AppStrings.PULSEMAN
import com.toasttab.pulseman.AppStrings.SAVE
import com.toasttab.pulseman.AppStrings.SAVE_AS
import com.toasttab.pulseman.AppStrings.UNSAVED_CHANGES_DIALOG_MESSAGE
import com.toasttab.pulseman.AppStrings.UNSAVED_CHANGES_DIALOG_TITLE
import com.toasttab.pulseman.view.tabHolderUI
import javax.swing.JFrame
import javax.swing.JOptionPane

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
fun main() = application {
    val appState = remember {
        try {
            AppState().apply {
                loadFile(true)
            }
        } catch (ex: Exception) {
            AppState().apply {
                loadDefault("$FAILED_TO_LOAD_PROJECT:\n$ex")
            }
        }
    }
    val tabs = appState.requestTabs.tabState
    val activeTab = appState.requestTabs.active
    val openTab = appState.requestTabs::open

    val promptForUnsavedChanges: (() -> Unit) = {
        if (appState.requestTabs.hasUnsavedChanges()) {
            val result = JOptionPane.showConfirmDialog(
                JFrame(),
                UNSAVED_CHANGES_DIALOG_MESSAGE,
                UNSAVED_CHANGES_DIALOG_TITLE,
                JOptionPane.YES_NO_CANCEL_OPTION
            )
            when (result) {
                JOptionPane.YES_OPTION -> {
                    appState.save(true)
                    exitApplication()
                }
                JOptionPane.NO_OPTION -> {
                    exitApplication()
                }
            }
        } else {
            exitApplication()
        }
    }
    
    Window(
        onCloseRequest = promptForUnsavedChanges,
        title = PULSEMAN,
        icon = painterResource(PULSE_LOGO),
        state = rememberWindowState(width = 900.dp, height = 1200.dp)
    ) {
        MaterialTheme(colors = AppTheme.colors.material) {
            Surface {
                Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                    tabHolderUI(tabs.map { it.toTab() }, activeTab?.toTab(), openTab)
                }
            }
        }
        MenuBar {
            Menu(FILE) {
                Item(
                    text = SAVE,
                    onClick = { appState.save(true) },
                    shortcut = KeyShortcut(key = Key.S, meta = true)
                )
                Item(text = SAVE_AS, onClick = { appState.save(false) })
                Item(
                    text = LOAD_PROJECT,
                    onClick = { appState.loadFile(false) },
                    shortcut = KeyShortcut(key = Key.L, meta = true)
                )
                Item(text = COPY_CURRENT_TAB, onClick = { appState.copyCurrentTab() })
            }
        }
    }
}
