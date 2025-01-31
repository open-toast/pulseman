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

import com.toasttab.pulseman.files.FileManagement.loadedJars
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Handles zipping and unzipping project files
 *
 * For saving:
 * It will take each tabs settings as a json string and store as a string file,
 * and store each jar type to its own zip folder.
 *
 * For loading:
 * Unzips a project file it will take all the zipped jar files and copy them as they are out of the zip
 * and return the json configuration for each tab.
 */
class ZipManagement(private val homeDirectory: String) {
    fun zipProject(tabsJson: String, zipName: String, jarFolders: List<File>) {
        FileOutputStream(File(zipName)).use { fos ->
            BufferedOutputStream(fos).use { bos ->
                ZipOutputStream(bos).use { zos ->
                    // Save json file
                    val jsonEntry = ZipEntry(projectTabsFileName)
                    zos.putNextEntry(jsonEntry)
                    zos.write(tabsJson.toByteArray())
                    zos.closeEntry()

                    // Save loaded jars
                    jarFolders.forEach { jarFolder ->
                        loadedJars(jarFolder).forEach { jar ->
                            FileInputStream(jar).use { input ->
                                BufferedInputStream(input).use { origin ->
                                    val entry = ZipEntry(savePath(jar))
                                    zos.putNextEntry(entry)
                                    origin.copyTo(zos, 1024)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun savePath(file: File) = File(homeDirectory).toURI().relativize(file.toURI()).path

    fun unzipProject(zippedFile: File): String? {
        var projectJson: String? = null
        FileInputStream(zippedFile).use { fis ->
            BufferedInputStream(fis).use { bis ->
                ZipInputStream(bis).use { zis ->
                    while (true) {
                        val nextEntry = zis.nextEntry ?: return projectJson
                        if (!nextEntry.isDirectory && nextEntry.name !in skipFiles) {
                            val newFile = File("$homeDirectory${nextEntry.name}")
                            val parentFile: File? = newFile.parentFile
                            if (parentFile?.exists() == true && isValidFolderPrefix(parentFile.name)) {
                                parentFile.mkdirs()
                            }
                            if (nextEntry.name == projectTabsFileName) {
                                projectJson = String(zis.readAllBytes())
                            } else {
                                val newFile = File("$homeDirectory${nextEntry.name}")
                                BufferedOutputStream(FileOutputStream(newFile)).use { bos ->
                                    val bytesIn = ByteArray(1024)
                                    var read: Int
                                    while (zis.read(bytesIn).also { read = it } != -1) {
                                        bos.write(bytesIn, 0, read)
                                    }
                                }
                            }
                            zis.closeEntry()
                        }
                    }
                }
            }
        }
    }

    private fun isValidFolderPrefix(folderName: String) = allowedFolder.any { folderName.startsWith(it) }

    companion object {
        private const val projectTabsFileName = "project_tabs.json"
        private val skipFiles = listOf(".DS_Store")
        private val allowedFolder = listOf("message_jars")
    }
}
