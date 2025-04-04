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

import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.JarLoaderType
import com.toasttab.pulseman.pulsar.filters.ClassFilter
import java.net.URL

/**
 * Stores a set of classes T, classes are loaded from a jar file using the classFilters supplied
 *
 * @param T the type of class that will be filtered for
 * @param classFilters this is a list of class filters used to pull any matching classes from a jar file
 * @param runTimeJarLoader the RunTimeJarLoader object that contains the jars we want to search for classes
 */
class LoadedClasses<T : ClassInfo>(
    private val classFilters: List<ClassFilter<T>>,
    private val runTimeJarLoader: RunTimeJarLoader
) {
    private val classes = mutableMapOf<String, T>()
    private var storedURLs: MutableList<URL> = mutableListOf()
    private var storedURLSet: MutableSet<String> = mutableSetOf()

    private fun generateURLs(): MutableList<URL> =
        runTimeJarLoader.getJarLoader(JarLoaderType.BASE).urLs.toMutableList()

    private fun MutableList<URL>.toStringSet(): MutableSet<String> = this.map { it.toString() }.toMutableSet()

    private fun generateClasses(): MutableMap<String, T> {
        val currentURLs = generateURLs()
        val currentURLSet = currentURLs.toStringSet()

        if (storedURLSet == currentURLSet) {
            return classes
        }
        classes.clear()
        storedURLs = currentURLs
        storedURLSet = currentURLSet

        currentURLs.forEach { url ->
            classFilters.forEach { filter ->
                filter.getClasses(url).forEach { cls ->
                    classes[cls.cls.name] = cls
                }
            }
        }
        return classes
    }

    fun getClass(className: String): T? = generateClasses()[className]

    fun filter(nameFilter: String): List<T> =
        generateClasses().values.filter { it.cls.name.contains(nameFilter, true) }.sortedBy { it.cls.name }

    fun doesJarContainValidClasses(url: URL): Boolean {
        // If there are no filters assume all classes are valid
        if (classFilters.isEmpty()) return true

        // Return early if any filter finds classes
        classFilters.forEach { filter ->
            if (filter.getClasses(url).isNotEmpty()) return true
        }

        return false
    }
}
