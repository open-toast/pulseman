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

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

fun Modifier.addSeparator(index: Int): Modifier {
    return this.drawBehind {
        val strokeWidth = Stroke.DefaultMiter / 2
        if (index != 0) {
            val oneQuarter = size.height / 4
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, oneQuarter),
                end = Offset(0f, size.height - oneQuarter),
                strokeWidth = strokeWidth
            )
        }
    }
}
