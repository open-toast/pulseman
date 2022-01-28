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

import com.toasttab.protokt.rt.KtMessage
import com.toasttab.pulseman.pulsar.handlers.KTMessageHandler
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.io.File
import java.net.URLClassLoader

/**
 * Filters for classes that implement the KtMessage protobuf message interface
 * https://github.com/open-toast/protokt/blob/main/protokt-runtime/src/main/kotlin/com/toasttab/protokt/rt/KtMessage.kt
 */
class KTMessageFilter : ClassFilter<PulsarMessage> {
    override fun getClasses(file: File): Set<KTMessageHandler> {
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()))
        return Reflections(classLoader.urLs)
            .get(Scanners.SubTypes.of(KtMessage::class.java).asClass<Any>(classLoader))
            .filterIsInstance<Class<out KtMessage>>()
            .map { KTMessageHandler(it, file) }
            .toSet()
    }
}
