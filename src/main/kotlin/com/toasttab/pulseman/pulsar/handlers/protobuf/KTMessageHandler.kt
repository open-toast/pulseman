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
import com.toasttab.pulseman.entities.JarLoaderType
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.DefaultMapper
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo

data class KTMessageHandler(
    override val cls: Class<out KtMessage>,
    override val runTimeJarLoader: RunTimeJarLoader
) : PulsarMessageClassInfo {

    override fun serialize(cls: Any): ByteArray {
        val ktMessage = cls as KtMessage
        return ktMessage.serialize()
    }

    override fun deserialize(bytes: ByteArray): Any {
        return try {
            cls.declaredClasses
                .first { it.name.contains(DESERIALIZER_CLASS) }
                .let {
                    val deserializer = getJarLoader().loadClass(it.name).kotlin.objectInstance as KtDeserializer<*>
                    deserializer.deserialize(bytes)
                }
        } catch (ex: Throwable) {
            "$EXCEPTION:$ex"
        }
    }

    override fun prettyPrint(cls: Any): String =
        DefaultMapper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cls)

    override fun generateClassTemplate(): String {
        val fullName = cls.name.replace("$", ".")
        val className = fullName.split(".").last()
        val importSet = mutableSetOf<String>()
        importSet.add(fullName)

        val variables = StringBuilder()
        getJarLoader()
            .loadClass(cls.name)
            .declaredFields
            .filter {
                it.name !in IGNORE_FIELDS
            }
            .forEach {
                if (!it.type.isPrimitive) {
                    importSet.add(it.type.name.replace("$", "."))
                }
                variables.appendLine("\t${it.name} = //$TODO")
            }
        val imports = StringBuilder()
        importSet.sorted().forEach { imports.appendLine("$IMPORT $it") }

        return "$imports\n$className {\n$variables}"
    }

    override fun generateFilterTemplate(): String {
        val fullName = cls.name.replace("$", ".")
        val className = fullName.split(".").last()
        val importSet = mutableSetOf<String>()
        importSet.add(fullName)

        val jarLoader = getJarLoader()
        val clazz = jarLoader.loadClass(cls.name)
        val fields = clazz.declaredFields.filter { it.name !in IGNORE_FIELDS }

        val visited = mutableSetOf<String>()
        visited.add(cls.name)
        val fieldLines = generateFieldLines(fields, "body", jarLoader, visited)

        val imports = importSet.sorted().joinToString("\n") { "$IMPORT $it" }

        return "$imports\n\n// Must return a Boolean\n{ body: $className ->\n${fieldLines.ifEmpty { "    true" }}\n}"
    }

    private fun generateFieldLines(
        fields: List<java.lang.reflect.Field>,
        accessor: String,
        jarLoader: JarLoader,
        visited: MutableSet<String>,
        nullable: Boolean = false
    ): String {
        val sep = if (nullable) "?." else "."
        return fields.joinToString(" &&\n") { field ->
            val fieldAccess = "$accessor$sep${field.name}"
            when {
                Map::class.java.isAssignableFrom(field.type) ->
                    "    $fieldAccess?.entries?.all { true } != false"
                List::class.java.isAssignableFrom(field.type) || Iterable::class.java.isAssignableFrom(field.type) -> {
                    val elementType = resolveListElementType(field)
                    if (elementType != null && KtMessage::class.java.isAssignableFrom(elementType) && elementType.name !in visited) {
                        visited.add(elementType.name)
                        val nested = jarLoader.loadClass(elementType.name).declaredFields.filter { it.name !in IGNORE_FIELDS }
                        val inner = generateFieldLines(nested, "it", jarLoader, visited, nullable = false)
                        if (inner.isEmpty()) {
                            "    $fieldAccess?.all { true } != false"
                        } else {
                            "    $fieldAccess?.all {\n$inner\n    } != false"
                        }
                    } else {
                        "    $fieldAccess?.all { it == ${defaultValue(field.type)} } != false"
                    }
                }
                KtMessage::class.java.isAssignableFrom(field.type) -> {
                    if (field.type.name in visited) {
                        "    true"
                    } else {
                        visited.add(field.type.name)
                        val nested = jarLoader.loadClass(field.type.name).declaredFields.filter { it.name !in IGNORE_FIELDS }
                        val inner = generateFieldLines(nested, fieldAccess, jarLoader, visited, nullable = true)
                        inner.ifEmpty { "    true" }
                    }
                }
                else -> "    $fieldAccess == ${defaultValue(field.type)}"
            }
        }
    }

    private fun defaultValue(type: Class<*>): String {
        return when (type.name) {
            "java.lang.String", "kotlin.String" -> "\"\""
            "boolean", "java.lang.Boolean", "kotlin.Boolean" -> "false"
            "int", "java.lang.Integer", "kotlin.Int" -> "0"
            "long", "java.lang.Long", "kotlin.Long" -> "0L"
            "float", "java.lang.Float", "kotlin.Float" -> "0.0f"
            "double", "java.lang.Double", "kotlin.Double" -> "0.0"
            else -> if (type.isEnum) "\"${type.enumConstants?.firstOrNull() ?: ""}\"" else "TODO()"
        }
    }

    private fun resolveListElementType(field: java.lang.reflect.Field): Class<*>? {
        val genericType = field.genericType as? java.lang.reflect.ParameterizedType ?: return null
        val typeArg = genericType.actualTypeArguments.firstOrNull() ?: return null
        return typeArg as? Class<*>
    }

    private val cachedJarLoader by lazy { runTimeJarLoader.getJarLoader(jarLoaderType = JarLoaderType.PROTOKT) }

    override fun getJarLoader(): JarLoader = cachedJarLoader

    companion object {
        private const val IMPORT = "import"
        private const val DESERIALIZER_CLASS = "Deserializer"
        private val IGNORE_FIELDS = listOf(
            "\$\$delegatedProperties",
            "messageSize\$delegate",
            "unknownFields",
            "Deserializer",
            "\$stable"
        )
    }
}
