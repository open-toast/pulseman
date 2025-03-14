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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.random.Random.Default.nextBytes

class ZipManagementTest {

    private class FileAndContent(val file: File, val contents: ByteArray)

    private lateinit var zipManagement: ZipManagement
    private lateinit var generatedJarFiles: List<FileAndContent>
    private lateinit var invalidJarFiles: List<FileAndContent>
    private lateinit var savedZipFile: File

    private val inJson = """
            {
                "val": 1
            }
    """.trimIndent()

    private fun createFile(dir: File, path: String): FileAndContent {
        val file = File(dir, path)
        val contents = nextBytes(ByteArray(100))
        file.writeBytes(contents)
        return FileAndContent(file, contents)
    }

    private fun validDirectories(tempDir: File) = mapOf(
        "jar0" to File(tempDir, "message_jars"),
        "jar1" to File(tempDir, "message_jars_0"),
        "jar2" to File(tempDir, "message_jars_1"),
        "jar4" to File(tempDir, "auth_jars"),
        "jar5" to File(tempDir, "dependency_jars")
    )

    private fun invalidDirectories(tempDir: File) = mapOf(
        "jar6" to File(tempDir, "dont_recreate_this"),
        "jar7" to File(tempDir, "misname_auth_jars")
    )

    private fun fileMap(directories: Map<String, File>) = directories.map {
        createFile(it.value, it.key)
    }

    // Set up all the needed temp directories and files
    private fun setUpZipProject(tempDir: File) {
        val directories = validDirectories(tempDir)
        // Create the valid jar folders
        directories.forEach { directory ->
            directory.value.mkdir()
        }
        // Create the files to zip
        generatedJarFiles = fileMap(directories)

        // Create folders to be zipped that we don't support unzipping
        val invalidDirectories = invalidDirectories(tempDir)
        invalidDirectories.forEach { invalidDirectory ->
            invalidDirectory.value.mkdir()
        }
        invalidJarFiles = fileMap(invalidDirectories)

        // Create the zip file location
        savedZipFile = File(tempDir, "file.zip")

        // Zip the files
        val jarsToZip = generatedJarFiles.map { it.file } + invalidJarFiles.map { it.file }
        zipManagement = ZipManagement("${tempDir.path}/")
        zipManagement.zipProject(inJson, savedZipFile.absolutePath, jarsToZip)

        assertThat(tempDir.listFiles()).hasSize(generatedJarFiles.size + invalidJarFiles.size + 1)

        // Delete all folders in the tempDir, leaving only the zip file
        tempDir.listFiles()?.forEach {
            if (it.isDirectory) {
                it.deleteRecursively()
            }
        }

        assertThat(tempDir.listFiles()).hasSize(1)
    }

    @Test
    fun `Zip and unzip a project successfully`(@TempDir tempDir: File) {
        setUpZipProject(tempDir)
        var errorCount = 0
        // Unzip the project
        val outJson = zipManagement.unzipProject(savedZipFile) { errorCount++ }

        // Confirm the unzipped files are the same as the originals
        assertThat(inJson).isEqualTo(outJson)
        generatedJarFiles.forEach {
            assertTrue(it.file.exists())
            assertArrayEquals(it.file.readBytes(), it.contents)
        }

        // Confirm unsupported folders were not unzipped
        invalidJarFiles.forEach {
            assertFalse(it.file.exists())
        }

        // Confirm we logged the invalid folders that weren't created
        assertThat(errorCount).isEqualTo(invalidJarFiles.size)
    }
}
