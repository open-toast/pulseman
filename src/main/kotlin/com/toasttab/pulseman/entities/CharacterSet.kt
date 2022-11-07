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

package com.toasttab.pulseman.entities

// Taken from import java.nio.charset.Charset
enum class CharacterSet(val charSet: String) {
    US_ASCII("US-ASCII"),
    ISO_8859_1("ISO-8859-1"),
    UTF_8("UTF-8"),
    UTF_16BE("UTF-16BE"),
    UTF_16LE("UTF-16LE"),
    UTF_16("UTF-16"),
    BASE64("Base64");

    companion object {
        private val charSetMapping = values().associateBy { it.charSet }

        fun fromCharSet(charSet: String): CharacterSet = charSetMapping[charSet]!!
    }
}
