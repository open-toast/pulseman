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

package com.toasttab.pulseman.util

import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import java.awt.FileDialog as AwtFileDialog

class FileDialog(
    title: String,
    mode: FileDialogMode,
    directory: String? = null,
    file: String? = null,
    private val extensionFilters: List<String> = emptyList()
) {
    private val fileDialog: AwtFileDialog = AwtFileDialog(Frame(), title, mode.ordinal)

    init {
        directory?.let { fileDialog.directory = it }
        file?.let { fileDialog.file = it }

        if (extensionFilters.isNotEmpty()) {
            fileDialog.filenameFilter = FilenameFilter { _, name -> extensionFilters.any { name.endsWith(it) } }
        }
    }

    fun show() {
        fileDialog.isVisible = true
    }

    fun getSelectedFileOrNull(): File? {
        val directory: String? = fileDialog.directory
        val file: String? = fileDialog.file

        return if (directory == null || file == null) {
            null
        } else {
            File(directory).resolve(file)
        }
    }
}
