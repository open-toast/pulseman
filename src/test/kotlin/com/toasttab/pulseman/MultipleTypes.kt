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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

data class MultipleTypes(
    val byt: Byte = 127,
    val sh: Short = 32767,
    val i: Int = -1,
    val l: Long = 123456789L,
    val d: Double = 189.987,
    val f: Float = 9898989.989F,
    val b: Boolean = true,
    val c: Char = 'R',
    val enum: TestEnum = TestEnum.B,
    val list: List<String> = listOf("a b", "c d"),
    val set: Set<Int> = setOf(4, 3, 2, 1),
    val map: Map<Int, Char> = hashMapOf(1 to '1', 2 to 'b'),
    val time: LocalDateTime = LocalDateTime.MIN,
    val guid: UUID = UUID.fromString("18a8ef60-cf55-11eb-b8bc-0242ac130003"),
    val recursive: MultipleTypes? = MultipleTypes(recursive = null)
) : Serializable {
    fun toBytes(): ByteArray {
        ByteArrayOutputStream().use { byteStream ->
            ObjectOutputStream(byteStream).use { objectStream ->
                objectStream.writeObject(this)
                objectStream.flush()
                return byteStream.toByteArray()
            }
        }
    }

    companion object {
        fun fromBytes(bytes: ByteArray): MultipleTypes {
            ByteArrayInputStream(bytes).use { byteInputStream ->
                ObjectInputStream(byteInputStream).use { inputStream ->
                    return inputStream.readObject() as MultipleTypes
                }
            }
        }
    }
}
