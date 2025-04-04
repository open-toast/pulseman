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

package com.toasttab.pulseman.state

import androidx.compose.runtime.Composable
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.view.jarManagementUI
import java.io.File

class JarManagement<T : ClassInfo>(
    val jars: JarManager<T>,
    private val selectedClass: SingleSelection<T>?,
    val setUserFeedback: (String) -> Unit,
    val onChange: () -> Unit,
    private val fileManagement: FileManagement
) {
    fun onAddJar(jar: File) {
        jars.addJar(
            jar,
            setUserFeedback,
            onChange
        )
    }

    fun onRemoveJar(jar: File) {
        fileManagement.deleteFile(jar)
        jars.removeJar(jar, setUserFeedback, selectedClass, onChange)
    }

    fun onRemoveAllJars() {
        jars.deleteAllJars()
    }

    fun getUI(): @Composable () -> Unit {
        return {
            jarManagementUI(
                loadedJars = jars.loadedJars,
                jarFolder = jars.jarFolder,
                onRemoveJar = ::onRemoveJar,
                onAddJar = ::onAddJar,
                onRemoveAllJars = ::onRemoveAllJars,
                fileManagement = fileManagement
            )
        }
    }
}
