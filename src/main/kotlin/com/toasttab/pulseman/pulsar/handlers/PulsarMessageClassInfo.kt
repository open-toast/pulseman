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

package com.toasttab.pulseman.pulsar.handlers

import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import java.io.File

/**
 * Defines an interface for serializing, deserializing and generating kotlin templates of a specific pulsar
 * messaging class
 *
 * TODO make this interface more type safe
 */
interface PulsarMessageClassInfo : PulsarMessage, ClassInfo {
    override val file: File
    override val cls: Class<out Any>
    val runTimeJarLoader: RunTimeJarLoader

    /**
     * Generates a kotlin scripting code template for the class, this code will be used to create a pulsar message class
     *
     * @return A string template for the class.
     */
    fun generateClassTemplate(): String

    /**
     * protoKT has overridden the package path for types like google.type.Money we need
     * separate classloaders to prevent conflicts between these imports
     *     com.google.api.grpc:proto-google-common-protos
     *     com.toasttab.protokt.thirdparty:proto-google-common-protos
     *
     * @return JarLoader containing the jar needed to generate a class using kotlin scripting
     */
    fun getJarLoader(): JarLoader
}
