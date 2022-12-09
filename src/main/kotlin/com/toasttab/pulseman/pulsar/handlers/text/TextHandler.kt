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

package com.toasttab.pulseman.pulsar.handlers.text

import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.entities.CharacterSet
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import java.nio.charset.Charset
import java.util.Base64

data class TextHandler(val characterSet: CharacterSet) : PulsarMessage {
    override fun serialize(cls: Any): ByteArray {
        val msg = cls as String
        return when (characterSet) {
            CharacterSet.BASE64 -> Base64.getDecoder().decode(msg)
            else -> msg.toByteArray(Charset.forName(characterSet.charSet))
        }
    }

    override fun deserialize(bytes: ByteArray): Any {
        return try {
            when (characterSet) {
                CharacterSet.BASE64 -> Base64.getEncoder().encodeToString(bytes)
                else -> String(bytes, Charset.forName(characterSet.charSet))
            }
        } catch (ex: Throwable) {
            "$EXCEPTION: $ex"
        }
    }

    override fun prettyPrint(cls: Any): String = cls as String
}
