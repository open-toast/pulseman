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
import com.toasttab.pulseman.AppStrings.SELECT_JAR_FILE
import com.toasttab.pulseman.AppStrings.ZIP_FILE
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FileWriter
import java.io.FilenameFilter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileNameExtensionFilter

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
    private const val ZIP_EXTENSION = "zip"
    private const val JAR_EXTENSION = "jar"
    private const val MAC_OS = "Mac"
    private const val OS_PROPERTY = "os.name"
    private const val HOME_PROPERTY = "user.home"

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
        if (!file.exists() && !file.mkdir())
            throw Exception(FAILED_TO_CREATE_DIRECTORY)
    }

    fun addFileDialog(jarFolder: File, action: (File) -> Unit) {
        val fileDialog = FileDialog(Frame(), "Select JAR files", FileDialog.LOAD)
        fileDialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".jar") }
        fileDialog.isVisible = true

        val directory: String? = fileDialog.directory
        val file: String? = fileDialog.file

        if (directory == null || file == null) {
            return
        }

        val selectedFile = File(directory).resolve(file)
        val copiedFile = File(jarFolder, selectedFile.absolutePath)

        selectedFile.copyTo(copiedFile, true)

        action(copiedFile)
    }

    fun deleteFile(file: File) {
        if (file.exists())
            file.delete()
    }

    fun saveProject(tabsJson: String, quickSave: Boolean, jarFolders: List<File>) {
        val filename = if (!quickSave) {
            val fileDialog = FileDialog(Frame(), "Save Project", FileDialog.SAVE)
            fileDialog.filenameFilter = FilenameFilter { _, name -> name.endsWith(".zip") }
            fileDialog.directory = projectFolder.absolutePath
            fileDialog.file = "pulseman-project.zip"
            fileDialog.isVisible = true

            val directory: String? = fileDialog.directory
            val file: String? = fileDialog.file

            if (directory != null && file != null) {
                val saveFile = File(directory).resolve(file)

                if (saveFile.absolutePath.endsWith(".zip")) {
                    saveFile.absolutePath
                } else {
                    saveFile.absolutePath + ".zip"
                }
            } else {
                null
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
            val file = JFileChooser().apply {
                currentDirectory = projectFolder
                fileSelectionMode = JFileChooser.FILES_ONLY
                fileFilter = FileNameExtensionFilter(ZIP_FILE, ZIP_EXTENSION)
            }

            // Need to get a JFrame to use swing
            val frame = JFrame()

            when (file.showOpenDialog(frame)) {
                JFileChooser.APPROVE_OPTION -> {
                    file.selectedFile
                }
                else -> null
            }
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
        } else null
    }

    fun loadedJars(jarFolder: File) = jarFolder.walk().filter { it.isFile }.toMutableSet()
}
