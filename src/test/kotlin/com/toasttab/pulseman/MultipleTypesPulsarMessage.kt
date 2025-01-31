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

import com.toasttab.pulseman.entities.JarLoaderType
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.DefaultMapper
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import java.io.File

class MultipleTypesPulsarMessage(
    override val cls: Class<out MultipleTypes>,
    override val file: File,
    override val runTimeJarLoader: RunTimeJarLoader
) :
    PulsarMessageClassInfo {
    override fun serialize(cls: Any): ByteArray = (cls as MultipleTypes).toBytes()

    override fun deserialize(bytes: ByteArray): Any = MultipleTypes.fromBytes(bytes)

    override fun prettyPrint(cls: Any): String =
        DefaultMapper.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cls)

    override fun generateClassTemplate(): String = "MultipleTypes()"

    override fun getJarLoader(): JarLoader {
        return runTimeJarLoader.getJarLoader(jarLoaderType = JarLoaderType.BASE)
    }
}
