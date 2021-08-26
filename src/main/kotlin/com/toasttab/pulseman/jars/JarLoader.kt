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
import java.net.URLClassLoader

/**
 * JarLoader extends URLClassLoader so it can access the protected addURL function and add jars to the classloader
 * Note: These added jars are only available to JarLoader not the inherited parent ClassLoader.
 *
 * TODO there may be a better way to do this with ModuleLayer
 * https://docs.oracle.com/javase/9/docs/api/java/lang/ModuleLayer.html
 */
class JarLoader(urls: Array<URL?>, parent: ClassLoader) : URLClassLoader(urls, parent) {
    fun addJar(url: URL) {
        super.addURL(url)
    }
}
