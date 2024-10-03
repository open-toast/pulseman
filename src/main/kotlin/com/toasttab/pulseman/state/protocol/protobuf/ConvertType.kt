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

package com.toasttab.pulseman.state.protocol.protobuf

import com.toasttab.pulseman.AppStrings.CONVERTING
import java.util.Base64

enum class ConvertType {
    BASE64,
    HEX;

    fun toBytes(value: String, setUserFeedback: (String) -> Unit): ByteArray {
        return when (this) {
            HEX -> {
                value
                    .filter { it.isLetterOrDigit() }
                    .uppercase()
                    .also { setUserFeedback("$CONVERTING:$it") }
                    .chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray()
            }

            BASE64 -> {
                value
                    .filter { !it.isWhitespace() }
                    .also { setUserFeedback("$CONVERTING:$it") }
                    .let { Base64.getDecoder().decode(it) }
            }
        }
    }
}
