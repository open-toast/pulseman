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
    private val extensionFilters: List<String> = emptyList(),
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
