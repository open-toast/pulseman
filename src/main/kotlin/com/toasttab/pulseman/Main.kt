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

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.v1.KeyStroke
import androidx.compose.ui.window.v1.Menu
import androidx.compose.ui.window.v1.MenuBar
import androidx.compose.ui.window.v1.MenuItem
import com.toasttab.pulseman.view.tabHolderUI
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
fun main() {
    val appState = AppState()
    appState.loadFile(true)

    // To use Apple global menu.
    System.setProperty("apple.laf.useScreenMenuBar", "true")

    AppManager.setMenu(
        MenuBar(
            Menu(
                name = "File",
                MenuItem(
                    name = "Save",
                    onClick = {
                        appState.save(true)
                    },
                    shortcut = KeyStroke(Key.S)
                ),
                MenuItem(
                    name = "Save as",
                    onClick = {
                        appState.save(false)
                    }
                ),
                MenuItem(
                    name = "Load project",
                    onClick = {
                        appState.loadFile(false)
                    },
                    shortcut = KeyStroke(Key.L)
                ),
                MenuItem(
                    name = "Copy current tab",
                    onClick = { appState.copyCurrentTab() }
                )
            )
        )
    )

    Window(title = "Pulseman", size = IntSize(900, 1200), icon = getWindowIcon()) {
        MaterialTheme(colors = AppTheme.colors.material) {
            DesktopTheme {
                Surface {
                    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                        tabHolderUI(appState.requestTabs)
                    }
                }
            }
        }
    }
}

private fun getWindowIcon() = try {
    Thread.currentThread().contextClassLoader.getResourceAsStream("pulse.png")?.let {
        ImageIO.read(it)
    }
} catch (e: Throwable) {
    null
} ?: BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
