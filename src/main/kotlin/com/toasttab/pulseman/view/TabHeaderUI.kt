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

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toasttab.pulseman.AppStrings.ADD_TAB
import com.toasttab.pulseman.AppStrings.CLOSE_TAB
import com.toasttab.pulseman.AppStrings.ELLIPSIS
import com.toasttab.pulseman.AppTheme
import com.toasttab.pulseman.state.Tab
import java.awt.event.MouseEvent

/**
 * This holds all the tabState in the project and allows the user to add or remove tabState.
 */
@ExperimentalComposeUiApi
@Composable
fun tabHeaderUI(tabs: List<Tab>, openTab: (String?) -> Unit) {
    val stateHorizontal = rememberScrollState(0)
    Row {
        Row(modifier = Modifier.horizontalScroll(stateHorizontal).weight(0.95f)) {
            tabs.forEach { tab ->
                Surface(
                    color = if (tab.isActive) AppTheme.colors.backgroundDark else Color.Transparent,
                    modifier = Modifier.pointerInput(tab) {
                        forEachGesture {
                            awaitPointerEventScope {
                                awaitPointerEvent().awtEventOrNull?.let { mouseEvent ->
                                    if (mouseEvent.button == MouseEvent.BUTTON2 && mouseEvent.id == MouseEvent.MOUSE_PRESSED) {
                                        tab.close()
                                    }
                                }
                            }
                        }
                    }.pointerMoveFilter(
                        onEnter = {
                            tab.onFocusedUpdate(true)
                            false
                        },
                        onExit = {
                            tab.onFocusedUpdate(false)
                            false
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.clickable(onClick = { tab.activate() }).padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when (tab.tabName.length) {
                                in 0..20 -> tab.tabName
                                else -> tab.tabName.take(18) + ELLIPSIS
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            modifier = Modifier.padding(4.dp)
                        )

                        if (tab.unsavedChanges) {
                            Icon(
                                imageVector = tab.image,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp)
                                    .clickable { tab.close() }
                                    .pointerMoveFilter(
                                        onEnter = {
                                            tab.onEnterIconUnsavedChanges()
                                            false
                                        },
                                        onExit = {
                                            tab.onExitIconUnsavedChanges()
                                            false
                                        }
                                    ).then(
                                        if (tab.drawBackground)
                                            Modifier.background(
                                                AppTheme.colors.backgroundLight,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        else
                                            Modifier
                                    ),
                                contentDescription = CLOSE_TAB,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(4.dp)
                                    .clickable { tab.close() }
                                    .alpha(if (tab.focused) 1f else 0f)
                                    .pointerMoveFilter(
                                        onEnter = {
                                            tab.onEnterIcon()
                                            false
                                        },
                                        onExit = {
                                            tab.onExitIcon()
                                            false
                                        }
                                    ).then(
                                        if (tab.drawBackground)
                                            Modifier.background(
                                                AppTheme.colors.backgroundLight,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                        else
                                            Modifier
                                    ),
                                contentDescription = CLOSE_TAB,
                            )
                        }
                    }
                }
            }
        }
        Icon(
            imageVector = Icons.Default.Add,
            modifier = Modifier
                .size(30.dp)
                .padding(4.dp)
                .weight(0.05f)
                .clickable { openTab(null) },
            contentDescription = ADD_TAB
        )
    }
    HorizontalScrollbar(
        adapter = rememberScrollbarAdapter(stateHorizontal)
    )
}
