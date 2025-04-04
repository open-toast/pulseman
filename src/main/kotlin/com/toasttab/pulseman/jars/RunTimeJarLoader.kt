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

import com.toasttab.pulseman.entities.JarLoaderType
import java.net.URL

/**
 * Manages a collection of jars that are loaded at runtime. Allows you to load a whole dependency tree of jar loaders.
 * In this project we have the several jar loaders dependent on each other:
 *
 * 1. General Jar loader, this has no dependencies and its jars will be used project wide.
 * 2. Auth Jar loader, this has a dependency on the general jar loader and will be used project wide.
 * 3. Tab Jar loader, this has a dependency on the general jar loader and the auth Jar loader. We will have one of
 *    these per tab so that we can have fully independent jars per tab and avoid conflicts.
 *
 * @param dependentJarLoader An optional dependent RunTimeJarLoader, any jars in this class will be added to the
 * JarLoader that is returned by getJarLoader().
 */
class RunTimeJarLoader(
    private val dependentJarLoader: RunTimeJarLoader? = null,
    // Using a list as a user might add the same jar multiple times in different jar dependency sections
    private val loadedJars: MutableList<URL> = mutableListOf()
) {

    fun addJar(url: URL): RunTimeJarLoader {
        loadedJars.add(url)
        return this
    }

    fun removeJar(url: URL) {
        loadedJars.remove(url)
    }

    // This is a recursive function that will load all the jars in the dependency tree
    private fun loadJars(runTimeJarLoader: RunTimeJarLoader, jarLoader: JarLoader): JarLoader {
        runTimeJarLoader.loadedJars.forEach { url ->
            jarLoader.addJar(url)
        }
        runTimeJarLoader.dependentJarLoader?.let { dependentJarLoader ->
            loadJars(runTimeJarLoader = dependentJarLoader, jarLoader = jarLoader)
        }
        return jarLoader
    }

    /**
     * Get a JarLoader that contains all the jars in the dependency tree.
     * This also supports adding jars specifically for Google Standard and ProtoKT jars which share the same namespace
     * and can lead to conflicts.
     *
     * Before returning the JarLoader, the current thread's context class loader is set to the JarLoader.
     * Things like scripting and serialization/deserialization wouldnt have access to the needed classes otherwise.
     *
     * @param jarLoaderType The type of jar loader to get.
     * @return A JarLoader that contains all the jars in the dependency tree.
     */
    fun getJarLoader(jarLoaderType: JarLoaderType): JarLoader {
        val jarLoader = loadJars(runTimeJarLoader = this, jarLoader = JarLoader(arrayOfNulls(0)))
        when (jarLoaderType) {
            JarLoaderType.BASE -> {}
            JarLoaderType.GOOGLE_STANDARD -> SeperatedJars.addGoogleJars(jarLoader)
            JarLoaderType.PROTOKT -> SeperatedJars.addProtoKTJars(jarLoader)
        }
        Thread.currentThread().contextClassLoader = jarLoader
        return jarLoader
    }
}
