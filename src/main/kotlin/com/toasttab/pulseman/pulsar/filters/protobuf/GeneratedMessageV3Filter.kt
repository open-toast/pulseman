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

package com.toasttab.pulseman.pulsar.filters.protobuf

import com.google.protobuf.GeneratedMessageV3
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.filters.ClassFilter
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.pulsar.handlers.protobuf.GeneratedMessageV3Handler
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.net.URL
import java.net.URLClassLoader

/**
 * Filters for classes that implement the GeneratedMessageV3 protobuf message interface
 * https://www.javadoc.io/static/com.google.protobuf/protobuf-java/3.5.1/com/google/protobuf/GeneratedMessageV3.html
 */
class GeneratedMessageV3Filter(private val runTimeJarLoader: RunTimeJarLoader) : ClassFilter<PulsarMessageClassInfo> {
    override fun getClasses(url: URL): Set<GeneratedMessageV3Handler> {
        val classLoader = URLClassLoader(arrayOf(url))
        return Reflections(classLoader.urLs)
            .get(Scanners.SubTypes.of(GeneratedMessageV3::class.java).asClass<Any>(classLoader))
            .filterIsInstance<Class<out GeneratedMessageV3>>()
            .map { GeneratedMessageV3Handler(cls = it, runTimeJarLoader = runTimeJarLoader) }
            .toSet()
    }
}
