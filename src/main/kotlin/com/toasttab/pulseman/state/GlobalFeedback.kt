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

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class GlobalFeedback {
    private val storedFeedback: SnapshotStateList<String> = mutableStateListOf()
    private val userFeedbackSet = mutableSetOf<UserFeedback>()

    fun registerCallback(userFeedback: UserFeedback, newTab: Boolean) {
        synchronized(this) {
            userFeedbackSet.add(userFeedback)
            if (!newTab) {
                replayStoredFeedback(userFeedback)
            }
        }
    }

    fun closeCallback(userFeedback: UserFeedback) {
        synchronized(this) {
            userFeedbackSet.remove(userFeedback)
        }
    }

    fun set(text: String) {
        synchronized(this) {
            if (userFeedbackSet.isEmpty()) {
                storedFeedback.add(text)
            } else {
                storedFeedback.clear()
                setUserFeedback(text)
            }
        }
    }

    fun reset() {
        synchronized(this) {
            userFeedbackSet.clear()
            storedFeedback.clear()
        }
    }

    private fun replayStoredFeedback(userFeedback: UserFeedback) {
        storedFeedback.forEach { feedback ->
            userFeedback.set(feedback)
        }
    }

    private fun setUserFeedback(text: String) {
        userFeedbackSet.forEach { userFeedback ->
            userFeedback.set(text)
        }
    }
}
