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

import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import org.apache.pulsar.client.api.Authentication
import org.reflections.Reflections
import java.io.File
import java.net.URLClassLoader

/**
 * Filters for classes that implement the Pulsar Authentication interface
 * https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/Authentication.html)
 */
class AuthClassFilter : ClassFilter<PulsarAuthHandler> {
    override fun getClasses(file: File): Set<PulsarAuthHandler> {
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()))
        val resultSet = mutableSetOf<PulsarAuthHandler>()
        Reflections(classLoader).getSubTypesOf(Authentication::class.java).forEach {
            resultSet.add(PulsarAuthHandler(it, file))
        }
        return resultSet
    }
}
