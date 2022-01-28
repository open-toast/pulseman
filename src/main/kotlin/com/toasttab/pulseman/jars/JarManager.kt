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

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.AppStrings.ADDED
import com.toasttab.pulseman.AppStrings.DELETED_CLASS_FEEDBACK
import com.toasttab.pulseman.AppStrings.REMOVED
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.files.FileManagement.APP_FOLDER_NAME
import com.toasttab.pulseman.files.FileManagement.appFolder
import com.toasttab.pulseman.files.FileManagement.deleteFile
import com.toasttab.pulseman.files.FileManagement.loadedJars
import com.toasttab.pulseman.files.FileManagement.makeFolder
import java.io.File

/**
 * Manages loaded jars and all the classes of Type T those jars contain.
 * When a jar is added to a project it is copied to the jarFolderName specified for this class.
 *
 * Any jars loaded are also added to the RunTimeJarLoader object to they can be available for serialization/
 * deserialization/authentication
 *
 * @param T the type of class the JarManager handles
 * @param loadedJars a list of all the jars loaded
 * @param loadedClasses a collection of the classes of Type T loaded in the jars
 * @param jarFolderName where all the loaded jars are stored
 */
data class JarManager<T : ClassInfo>(
    val loadedJars: SnapshotStateList<File> = mutableStateListOf(),
    val loadedClasses: LoadedClasses<T>,
    private val jarFolderName: String
) {
    private val jarFolderPath = "$jarFolderName/"
    val jarFolder = File("$APP_FOLDER_NAME$jarFolderPath")

    init {
        // This app folder creation will be repeated, as I'm wary of object creation order with FileManagement
        makeFolder(appFolder)
        makeFolder(jarFolder)
    }

    private fun addJar(jarFile: File) {
        loadedJars.add(jarFile)
        loadedClasses.addFile(jarFile)
        RunTimeJarLoader.addJar(jarFile.toURI().toURL())
    }

    private fun removeJar(jarFile: File) {
        loadedJars.remove(jarFile)
        loadedClasses.removeFile(jarFile)
        RunTimeJarLoader.removeJar(jarFile.toURI().toURL())
    }

    private fun clearAllJars() {
        loadedJars.forEach {
            RunTimeJarLoader.removeJar(it.toURI().toURL())
        }
        loadedJars.clear()
        loadedClasses.clear()
    }

    fun refresh() {
        clearAllJars()
        loadedJars(jarFolder).forEach {
            addJar(it)
        }
        sortLists()
    }

    private fun sortLists() {
        loadedJars.sortWith(compareBy { it.name })
    }

    fun addJar(file: File, setUserFeedback: (String) -> Unit, onChange: () -> Unit) {
        if (!loadedJars.contains(file)) {
            addJar(file)
            sortLists()
            setUserFeedback("$ADDED ${file.path}")
            onChange()
        }
    }

    fun removeJar(
        jar: File,
        setUserFeedback: (String) -> Unit,
        selectedClass: SingleSelection<T>?,
        onChange: () -> Unit
    ) {
        removeJar(jar)
        var feedback = "$REMOVED ${jar.path}"
        if (selectedClass?.selected?.file == jar) {
            feedback += "\n$DELETED_CLASS_FEEDBACK. ${selectedClass.selected?.cls?.name}"
            selectedClass.selected = null
        }
        setUserFeedback(feedback)
        onChange()
    }

    fun deleteAllJars() {
        refresh()
        loadedJars.forEach {
            deleteFile(it)
        }
        clearAllJars()
    }
}
