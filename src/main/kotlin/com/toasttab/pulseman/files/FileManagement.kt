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

import androidx.compose.desktop.AppManager
import java.io.File
import java.io.FileWriter
import javax.swing.JFileChooser
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
    const val appFolderName = "pulseman_config/"
    const val projectTabsFileName = "project_tabs.json"

    val appFolder = File(appFolderName)

    private const val projectsFolderName = "projects/"
    private const val lastConfigLoadedPath = "${appFolderName}last_config_loaded"
    private val projectFolder = File("$appFolderName$projectsFolderName")
    private val zipManager = ZipManagement()

    init {
        makeFolder(appFolder)
        makeFolder(projectFolder)
    }

    fun makeFolder(file: File) {
        if (!file.exists() && !file.mkdir())
            throw Exception("Failed to create directory")
    }

    fun addFileDialog(jarFolder: File, action: (File) -> Unit) {
        val file = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            fileFilter = FileNameExtensionFilter("Select Jar file", "jar")
        }

        // Need to get a JFrame to use swing
        val frame = AppManager.focusedWindow?.window?.contentPane ?: AppManager.windows.first().window.contentPane

        when (file.showOpenDialog(frame)) {
            JFileChooser.APPROVE_OPTION -> {
                val newFile = File(jarFolder, file.selectedFile.name)
                file.selectedFile.copyTo(newFile, true)
                action(newFile)
            }
            else -> return
        }
    }

    fun deleteFile(file: File) {
        if (file.exists())
            file.delete()
    }

    fun saveProject(tabsJson: String, quickSave: Boolean, jarFolders: List<File>) {
        val filename = if (!quickSave) {
            val fileChooser = JFileChooser().apply {
                currentDirectory = projectFolder
                fileFilter = FileNameExtensionFilter("Zip file", "zip")
            }

            // Need to get a JFrame to use swing
            val frame = AppManager.windows.first().window.contentPane

            when (fileChooser.showSaveDialog(frame)) {
                JFileChooser.APPROVE_OPTION -> {
                    val fileName = fileChooser.selectedFile.toString()
                    if (fileName.substringAfterLast(".") == "zip")
                        fileName
                    else
                        "$fileName.zip"
                }
                else -> null
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
                fileFilter = FileNameExtensionFilter("Zip file", "zip")
            }

            // Need to get a JFrame to use swing
            val frame = AppManager.windows.first().window.contentPane

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
