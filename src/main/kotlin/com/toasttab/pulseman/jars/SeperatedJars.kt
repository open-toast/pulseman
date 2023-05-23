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
 * protoKT has overridden the package path for types like google.type.Money we need
 * separate classloaders to prevent conflicts with these Jars
 */
object SeperatedJars {
    // Gradle task copies and names these jars to the resource folder
    private const val googleCommon = "proto-google-common-protos-original.jar"
    private const val protoKTCommon = "proto-google-common-protos-protoKT.jar"
    private const val protoKTCommonLite = "proto-google-common-protos-lite-protoKT.jar"

    private val googleJars = listOf(getJarURL(googleCommon))
    private val protoKTJars = listOf(getJarURL(protoKTCommon), getJarURL(protoKTCommonLite))

    private fun getJarURL(resourcePath: String): URL {
        val contextClassLoader = Thread.currentThread().contextClassLoader!!
        return contextClassLoader.getResource(resourcePath)!!
    }

    fun addGoogleJars(jarLoader: JarLoader) {
        googleJars.forEach {
            jarLoader.addJar(it)
        }
    }

    fun addProtoKTJars(jarLoader: JarLoader) {
        protoKTJars.forEach {
            jarLoader.addJar(it)
        }
    }
}
