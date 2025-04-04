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
import com.toasttab.pulseman.AppStrings.CONFLICT_WARNING
import com.toasttab.pulseman.AppStrings.DELETED_CLASS_FEEDBACK
import com.toasttab.pulseman.AppStrings.REMOVED
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.state.GlobalFeedback
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
 * @param originalJarFolderName used for migrating from project wide folders to each tab having its own folder
 */
data class JarManager<T : ClassInfo>(
    val loadedJars: SnapshotStateList<File> = mutableStateListOf(),
    val loadedClasses: LoadedClasses<T>,
    private val globalFeedback: GlobalFeedback,
    private val jarFolderName: String,
    val runTimeJarLoader: RunTimeJarLoader,
    val originalJarFolderName: String?,
    val tabFileExtension: Int?,
    val fileManagement: FileManagement
) {
    private val originalJarFolderPath = originalJarFolderName?.let { "$it/" }
    private val originalJarFolder =
        originalJarFolderPath?.let { File("${fileManagement.APP_FOLDER_NAME}$originalJarFolderPath") }
    private var isMigrated = originalJarFolder == null

    private val jarFolderPath = "$jarFolderName/"
    val jarFolder = File("${fileManagement.APP_FOLDER_NAME}$jarFolderPath")

    init {
        // This app folder creation will be repeated, as I'm wary of object creation order with FileManagement
        fileManagement.makeFolder(fileManagement.appFolder)
        fileManagement.makeFolder(jarFolder)
    }

    private fun addJar(jarFile: File, printError: Boolean) {
        checkForConflicts(file = jarFile, printError = printError)
        loadedJars.add(jarFile)
        runTimeJarLoader.addJar(jarFile.toURI().toURL())
    }

    private fun removeJar(jarFile: File) {
        loadedJars.remove(jarFile)
        runTimeJarLoader.removeJar(jarFile.toURI().toURL())
    }

    private fun clearAllJars() {
        loadedJars.forEach {
            runTimeJarLoader.removeJar(it.toURI().toURL())
        }
        loadedJars.clear()
    }

    fun refresh(printError: Boolean) {
        clearAllJars()
        fileManagement.loadedJars(jarFolder).forEach {
            addJar(jarFile = it, printError = printError)
        }
        migrateOldJarFolderFormat(printError = printError)
        sortLists()
    }

    private fun migrateOldJarFolderFormat(printError: Boolean) {
        if (originalJarFolder != null && loadedJars.isEmpty() && !isMigrated) {
            fileManagement.loadedJars(originalJarFolder).forEach { file ->
                val newFile = copyFile(file = file)
                addJar(jarFile = newFile, printError = printError)
            }
            isMigrated = true
        }
    }

    fun copyFile(file: File): File {
        val copiedFile = jarFolder.resolve(file.name)
        file.copyTo(target = copiedFile, overwrite = true)
        return copiedFile
    }

    private fun sortLists() {
        loadedJars.sortWith(compareBy { it.name })
    }

    private fun checkForConflicts(file: File, printError: Boolean) {
        if (printError && file.name.contains(CONFLICT_FILE_NAME)) {
            globalFeedback.set("$CONFLICT_WARNING: ${file.name}")
        }
    }

    fun addJar(file: File, setUserFeedback: (String) -> Unit, onChange: () -> Unit) {
        if (!loadedJars.contains(file)) {
            addJar(jarFile = file, printError = true)
            sortLists()
            setUserFeedback("$ADDED ${file.path}")
            onChange()
        }
    }

    fun copyJars(jarFiles: List<File>, printError: Boolean) {
        jarFiles.forEach { file ->
            addJar(jarFile = file, printError = printError)
            copyFile(file = file)
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
        if (selectedClass?.selected?.cls?.protectionDomain?.codeSource?.location?.path == jar.path) {
            feedback += "\n$DELETED_CLASS_FEEDBACK. ${selectedClass?.selected?.cls?.name}"
            selectedClass?.selected = null
        }
        setUserFeedback(feedback)
        onChange()
    }

    fun deleteAllJars() {
        refresh(printError = false)
        loadedJars.forEach {
            fileManagement.deleteFile(it)
        }
        originalJarFolder?.let {
            fileManagement.loadedJars(originalJarFolder).forEach {
                fileManagement.deleteFile(it)
            }
        }
        clearAllJars()
    }

    companion object {
        private const val CONFLICT_FILE_NAME = "proto-google-common-protos"
    }
}
