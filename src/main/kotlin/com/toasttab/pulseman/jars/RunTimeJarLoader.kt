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

package com.toasttab.pulseman.jars

import java.net.URL

/**
 * This is the projects global class loader, any jars that are added to the project are added to this object.
 * This makes serialization/deserialization/authentication with imported classes much easier to manage.
 */
object RunTimeJarLoader {
    // Using a list as a user might add the same jar multiple times in different jar dependency sections
    private val loadedJars = mutableListOf<URL>()

    fun addJar(url: URL) {
        loadedJars.add(url)
        jarLoader.addJar(url)
    }

    fun removeJar(url: URL) {
        loadedJars.remove(url)
        jarLoader = newJarLoader().apply {
            loadedJars.forEach { url ->
                this.addJar(url)
            }
        }
    }

    fun addJarsToClassLoader() {
        Thread.currentThread().contextClassLoader = jarLoader
    }

    private fun newJarLoader() = JarLoader(arrayOfNulls(0))

    private var jarLoader = newJarLoader()

    val loader: JarLoader
        get() = jarLoader

    val googleJarLoader: JarLoader
        get() = jarLoader.copy().also {
            SeperatedJars.addGoogleJars(it)
        }

    val protoKTJarLoader: JarLoader
        get() = jarLoader.copy().also {
            SeperatedJars.addProtoKTJars(it)
        }
}
