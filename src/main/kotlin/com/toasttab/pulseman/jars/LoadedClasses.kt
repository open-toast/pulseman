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
import com.toasttab.pulseman.pulsar.filters.ClassFilter
import java.io.File

/**
 * Stores a set of classes T, classes are loaded from a jar file using the classFilters supplied
 *
 * @param T the type of class that will be filtered for
 * @param classFilters this is a list of class filters used to pull any matching classes from a jar file
 */
class LoadedClasses<T : ClassInfo>(private val classFilters: List<ClassFilter<T>>) {
    private val classSet = mutableSetOf<T>()

    fun addFile(file: File) {
        classFilters.forEach {
            classSet.addAll(it.getClasses(file))
        }
    }

    fun removeFile(file: File) {
        classSet.removeIf { it.file == file }
    }

    fun getClass(className: String): T? = classSet.firstOrNull {
        it.cls.name == className
    }

    fun filter(nameFilter: String): List<T> =
        classSet.filter { it.cls.name.contains(nameFilter, true) }.sortedBy { it.cls.name }

    fun clear() {
        classSet.clear()
    }
}
