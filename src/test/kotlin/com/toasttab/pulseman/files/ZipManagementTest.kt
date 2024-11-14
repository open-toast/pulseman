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

    private fun createFile(dir: File, path: String): FileAndContent {
        val file = File(dir, path)
        val contents = nextBytes(ByteArray(100))
        file.writeBytes(contents)
        return FileAndContent(file, contents)
    }

    private fun directories(tempDir: File) = mapOf(
        "message" to File(tempDir, "message"),
        "auth" to File(tempDir, "auth"),
        "other" to File(tempDir, "other")
    )

    private fun fileMap(directories: Map<String, File>) = directories.map {
        createFile(it.value, it.key)
    }

    @Test
    fun `confirm tests fail is caught`() {
        assertTrue(false)
    }

    @Test
    fun `Zip and unzip a project successfully`(@TempDir tempDir: File) {
        // Set up all the needed temp directories and files
        val directories = directories(tempDir)
        directories.forEach {
            it.value.mkdir()
        }
        val generatedJarFiles = fileMap(directories)

        val saveDirectory = File(tempDir, "saved")
        saveDirectory.mkdir()
        val savedZipFile = File(saveDirectory, "file.zip")

        val inJson = """
            {
                "val": 1
            }
        """.trimIndent()

        // Zip the files
        val jarsToZip = generatedJarFiles.map { it.file }
        val zipManagement = ZipManagement("")
        zipManagement.zipProject(inJson, savedZipFile.absolutePath, jarsToZip)

        // Delete the original files
        jarsToZip.forEach {
            it.delete()
            assertFalse(it.exists())
        }

        // Unzip the project
        val outJson = zipManagement.unzipProject(savedZipFile)

        // Confirm the unzipped files are the same as the originals
        assertThat(inJson).isEqualTo(outJson)
        generatedJarFiles.forEach {
            assertTrue(it.file.exists())
            assertArrayEquals(it.file.readBytes(), it.contents)
        }
    }
}
