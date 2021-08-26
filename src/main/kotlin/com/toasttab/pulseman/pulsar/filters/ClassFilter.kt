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

import com.toasttab.pulseman.entities.ClassInfo
import java.io.File

/**
 * Defines an interface to filter a specific class type from a jar file
 *
 * @param T The ClassInfo type that will be filtered
 */
interface ClassFilter<T : ClassInfo> {
    /**
     * Should take the supplied jar file and return a set of filtered classes
     *
     * @param file the jar file that is to be searched
     * @return A set of T containing the list of filtered classes and their linked file.
     */
    fun getClasses(file: File): Set<T>
}
