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

package com.toasttab.pulseman.files

import com.toasttab.pulseman.AppStrings.FAILED_TO_CREATE_DIRECTORY
import com.toasttab.pulseman.AppStrings.JAR_FILE_DIALOG_TITLE
import com.toasttab.pulseman.AppStrings.PROJECT_FILE_DIALOG_TITLE
import com.toasttab.pulseman.AppStrings.SAVE_FILE_DIALOG_TITLE
import com.toasttab.pulseman.util.FileDialog
import com.toasttab.pulseman.util.FileDialogMode
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Handles all file operations
 * - Adding and removing jar files from the project.
 * - Saving and loading projects
 *
 * TODO. Do all file dialogs in compose
 * Using swing JFileChooser at the moment, I can't find any built in compose desktop file dialogs and don't
 * want to create my own. There will likely a way to do this soon in compose desktop.
 *
 * TODO pull all view logic out of here
 */
object FileManagement {
    private const val MAC_HOME_FOLDER = "/Library/Pulseman/"
    private const val CONFIG_DIRECTORY = "pulseman_config/"
    private const val LAST_CONFIG_USED_FILENAME = "last_config_loaded"
    private const val PROJECTS_FOLDER_NAME = "projects/"
    private const val ZIP_EXTENSION = ".zip"
    private const val JAR_EXTENSION = ".jar"
    private const val MAC_OS = "Mac"
    private const val OS_PROPERTY = "os.name"
    private const val HOME_PROPERTY = "user.home"
    private const val DEFAULT_PROJECT_NAME = "pulseman-project$ZIP_EXTENSION"

    private val os = System.getProperty(OS_PROPERTY)

    // Sand boxed mac apps only have access by default to this folder
    private val HOME_DIRECTORY = if (os.contains(MAC_OS)) System.getProperty(HOME_PROPERTY) + MAC_HOME_FOLDER else ""

    val APP_FOLDER_NAME = "${HOME_DIRECTORY}$CONFIG_DIRECTORY"
    val appFolder = File(APP_FOLDER_NAME)

    private val lastConfigLoadedPath = "${APP_FOLDER_NAME}$LAST_CONFIG_USED_FILENAME"
    private val projectFolder = File("$APP_FOLDER_NAME$PROJECTS_FOLDER_NAME")
    private val zipManager = ZipManagement(HOME_DIRECTORY)

    init {
        makeFolder(appFolder)
        makeFolder(projectFolder)
    }

    fun makeFolder(file: File) {
        val path: Path = Paths.get(file.toURI())
        Files.createDirectories(path)
        if (!file.exists() && !file.mkdir()) {
            throw Exception(FAILED_TO_CREATE_DIRECTORY)
        }
    }

    fun addFileDialog(jarFolder: File, action: (File) -> Unit) {
        val fileDialog = FileDialog(
            title = JAR_FILE_DIALOG_TITLE,
            mode = FileDialogMode.LOAD,
            extensionFilters = listOf(JAR_EXTENSION)
        )

        fileDialog.show()

        val selectedFile = fileDialog.getSelectedFileOrNull() ?: return
        val copiedFile = jarFolder.resolve(selectedFile.name)

        selectedFile.copyTo(copiedFile, true)

        action(copiedFile)
    }

    fun deleteFile(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }

    fun saveProject(tabsJson: String, quickSave: Boolean, jarFolders: List<File>) {
        val filename = if (!quickSave) {
            val fileDialog = FileDialog(
                title = SAVE_FILE_DIALOG_TITLE,
                mode = FileDialogMode.SAVE,
                extensionFilters = listOf(ZIP_EXTENSION),
                directory = projectFolder.absolutePath,
                file = DEFAULT_PROJECT_NAME
            )

            fileDialog.show()

            fileDialog.getSelectedFileOrNull()?.let {
                if (it.absolutePath.endsWith(ZIP_EXTENSION)) {
                    it.absolutePath
                } else {
                    it.absolutePath + ZIP_EXTENSION
                }
            }
        } else {
            getLastLoadedFile()?.absolutePath
        }
        filename?.let {
            zipManager.zipProject(tabsJson, filename, jarFolders)
            FileWriter(lastConfigLoadedPath).use { fw -> fw.write(filename) }
        }
    }

    fun getProjectFile(loadDefault: Boolean): File? {
        return if (loadDefault) {
            getLastLoadedFile()
        } else {
            val fileDialog = FileDialog(
                title = PROJECT_FILE_DIALOG_TITLE,
                mode = FileDialogMode.LOAD,
                extensionFilters = listOf(ZIP_EXTENSION),
                directory = projectFolder.absolutePath
            )

            fileDialog.show()

            return fileDialog.getSelectedFileOrNull()
        }
    }

    fun loadProject(file: File): String? {
        FileWriter(lastConfigLoadedPath).use { fw -> fw.write(file.absolutePath) }
        return zipManager.unzipProject(file)
    }

    private fun getLastLoadedFile(): File? = File(lastConfigLoadedPath).let {
        if (it.exists()) {
            val lastLoadedFile = File(it.readText())
            if (lastLoadedFile.exists()) lastLoadedFile else null
        } else {
            null
        }
    }

    fun loadedJars(jarFolder: File) = jarFolder.walk().filter { it.isFile }.toMutableSet()
}
