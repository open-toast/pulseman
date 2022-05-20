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

import com.toasttab.protokt.rt.KtDeserializer
import com.toasttab.protokt.rt.KtMessage
import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.AppStrings.TODO
import com.toasttab.pulseman.pulsar.handlers.DefaultMapper
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import java.io.File

data class KTMessageHandler(override val cls: Class<out KtMessage>, override val file: File) : PulsarMessageClassInfo {

    override fun serialize(cls: Any): ByteArray {
        val ktMessage = cls as KtMessage
        return ktMessage.serialize()
    }

    override fun deserialize(bytes: ByteArray): Any {
        return try {
            cls.declaredClasses
                .first { it.name.contains(DESERIALIZER_CLASS) }
                .let {
                    val deserializer = (it.kotlin.objectInstance as KtDeserializer<*>)
                    deserializer.deserialize(bytes)
                }
        } catch (ex: Throwable) {
            "$EXCEPTION:$ex"
        }
    }

    override fun prettyPrint(cls: Any): String =
        DefaultMapper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cls)

    override fun generateClassTemplate(): String {
        val className = cls.name.split(".").last()
        val importSet = mutableSetOf<String>()
        importSet.add(cls.name)

        val variables = StringBuilder()
        cls.declaredFields
            .filter {
                it.name !in IGNORE_FIELDS
            }
            .forEach {
                if (!it.type.isPrimitive)
                    importSet.add(it.type.name.replace("$", "."))
                variables.appendLine("\t${it.name} = //$TODO")
            }
        val imports = StringBuilder()
        importSet.sorted().forEach { imports.appendLine("$IMPORT $it") }

        return "$imports\n$className {\n$variables}"
    }

    companion object {
        private const val IMPORT = "import"
        private const val DESERIALIZER_CLASS = "Deserializer"
        private val IGNORE_FIELDS = listOf(
            "\$\$delegatedProperties",
            "messageSize\$delegate",
            "unknownFields",
            "Deserializer"
        )
    }
}
