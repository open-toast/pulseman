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

package com.toasttab.pulseman.pulsar.filters

import com.google.protobuf.GeneratedMessageV3
import com.toasttab.pulseman.pulsar.handlers.GeneratedMessageV3Handler
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import org.reflections.Reflections
import java.io.File
import java.net.URLClassLoader

/**
 * Filters for classes that implement the GeneratedMessageV3 protobuf message interface
 * https://www.javadoc.io/static/com.google.protobuf/protobuf-java/3.5.1/com/google/protobuf/GeneratedMessageV3.html
 */
class GeneratedMessageV3Filter : ClassFilter<PulsarMessage> {
    override fun getClasses(file: File): Set<GeneratedMessageV3Handler> {
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()))
        val resultSet = mutableSetOf<GeneratedMessageV3Handler>()
        Reflections(classLoader).getSubTypesOf(GeneratedMessageV3::class.java).map {
            resultSet.add(GeneratedMessageV3Handler(it, file))
        }
        return resultSet
    }
}
