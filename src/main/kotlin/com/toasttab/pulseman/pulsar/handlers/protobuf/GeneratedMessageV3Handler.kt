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

import com.google.protobuf.Descriptors.FieldDescriptor
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.util.JsonFormat
import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.entities.JarLoaderType
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo

data class GeneratedMessageV3Handler(
    override val cls: Class<out GeneratedMessageV3>,
    override val runTimeJarLoader: RunTimeJarLoader
) : PulsarMessageClassInfo {

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

    override fun generateFilterTemplate(): String {
        val fullName = cls.name.replace("$", ".")
        val className = fullName.split(".").last()
        val import = "import $fullName"

        val clazz = getJarLoader().loadClass(cls.name)
        val defaultInstance = clazz.getDeclaredMethod(GET_DEFAULT_INSTANCE_METHOD).invoke(null) as GeneratedMessageV3
        val fields = defaultInstance.descriptorForType.fields

        val visited = mutableSetOf<String>()
        val fieldLines = generateFieldLines(fields, "body", visited)

        return "$import\n\n// Must return a Boolean\n{ body: $className ->\n${fieldLines.ifEmpty { "    true" }}\n}"
    }

    private fun generateFieldLines(
        fields: List<FieldDescriptor>,
        accessor: String,
        visited: MutableSet<String>
    ): String {
        return fields.joinToString(" &&\n") { field ->
            when {
                field.isMapField -> "    $accessor.${field.jsonName}.entries.all { true }"
                field.isRepeated -> {
                    if (field.javaType == FieldDescriptor.JavaType.MESSAGE) {
                        val nested = field.messageType
                        if (nested.fullName in visited) {
                            "    $accessor.${field.jsonName}List.all { true }"
                        } else {
                            visited.add(nested.fullName)
                            val inner = generateFieldLines(nested.fields, "it", visited)
                            if (inner.isEmpty()) {
                                "    $accessor.${field.jsonName}List.all { true }"
                            } else {
                                "    $accessor.${field.jsonName}List.all {\n$inner\n    }"
                            }
                        }
                    } else {
                        "    $accessor.${field.jsonName}List.all { it == ${defaultValue(field)} }"
                    }
                }
                field.javaType == FieldDescriptor.JavaType.MESSAGE -> {
                    val nested = field.messageType
                    if (nested.fullName in visited) {
                        "    true"
                    } else {
                        visited.add(nested.fullName)
                        val inner = generateFieldLines(nested.fields, "$accessor.${field.jsonName}", visited)
                        inner.ifEmpty { "    true" }
                    }
                }
                else -> "    $accessor.${field.jsonName} == ${defaultValue(field)}"
            }
        }
    }

    private fun defaultValue(field: FieldDescriptor): String {
        return when (field.javaType) {
            FieldDescriptor.JavaType.STRING -> "\"\""
            FieldDescriptor.JavaType.BOOLEAN -> "false"
            FieldDescriptor.JavaType.INT -> "0"
            FieldDescriptor.JavaType.LONG -> "0L"
            FieldDescriptor.JavaType.FLOAT -> "0.0f"
            FieldDescriptor.JavaType.DOUBLE -> "0.0"
            FieldDescriptor.JavaType.ENUM -> "\"${field.enumType.values.firstOrNull()?.name ?: ""}\""
            FieldDescriptor.JavaType.BYTE_STRING -> "ByteString.EMPTY"
            else -> "TODO()"
        }
    }

    private val cachedJarLoader by lazy { runTimeJarLoader.getJarLoader(jarLoaderType = JarLoaderType.GOOGLE_STANDARD) }

    override fun getJarLoader(): JarLoader = cachedJarLoader

    companion object {
        private const val PARSE_METHOD = "parseFrom"
        private const val GET_DEFAULT_INSTANCE_METHOD = "getDefaultInstance"
    }
}
