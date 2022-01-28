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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.toasttab.pulseman.view.userFeedbackUI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserFeedback(private val userFeedback: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue())) {

    companion object {
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    fun setUserFeedback(text: String) {
        val appendNewLine = if (userFeedback.value.text.isNotEmpty()) "\n" else ""
        userFeedback.value = TextFieldValue(
            text = "${userFeedback.value.text}$appendNewLine${timeNow()}: $text"
        )
    }

    private fun onUserFeedbackClear() {
        userFeedback.value = TextFieldValue("")
    }

    private fun timeNow(): String = LocalDateTime.now().format(formatter)

    fun ui(): @Composable () -> Unit = {
        userFeedbackUI(
            userFeedback = userFeedback.value.text,
            onUserFeedbackClear = ::onUserFeedbackClear
        )
    }
}
