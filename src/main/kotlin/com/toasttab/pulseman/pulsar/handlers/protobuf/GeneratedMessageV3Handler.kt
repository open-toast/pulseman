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

package com.toasttab.pulseman.pulsar.handlers.protobuf

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.util.JsonFormat
import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import java.io.File

data class GeneratedMessageV3Handler(override val cls: Class<out GeneratedMessageV3>, override val file: File) :
    PulsarMessageClassInfo {

    override fun serialize(cls: Any): ByteArray {
        val generatedMessageV3 = cls as GeneratedMessageV3
        return generatedMessageV3.toByteArray()
    }

    override fun deserialize(bytes: ByteArray): Any {
        return try {
            getJarLoader()
                .loadClass(cls.name)
                .getDeclaredMethod(PARSE_METHOD, ByteArray::class.java)
                .invoke(null, bytes)
        } catch (ex: Throwable) {
            "$EXCEPTION:$ex"
        }
    }

    override fun prettyPrint(cls: Any): String =
        JsonFormat.printer().print(cls as GeneratedMessageV3)

    override fun generateClassTemplate(): String {
        val fullName = cls.name.replace("$", ".")
        val className = fullName.split(".").last()
        val import = "import $fullName"

        return "$import\n\n$className\n\t.newBuilder()\n\t//TODO set values\n\t.build()"
    }

    override fun getJarLoader(): JarLoader {
        return RunTimeJarLoader.googleJarLoader
    }

    companion object {
        private const val PARSE_METHOD = "parseFrom"
    }
}
