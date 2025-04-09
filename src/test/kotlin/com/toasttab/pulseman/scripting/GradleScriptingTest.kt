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

package com.toasttab.pulseman.scripting

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GradleScriptingTest {
    @Test
    fun `Gradle file download and clean up works`(@TempDir tempDir: File) {
        val taskName = "gradle_scripting_test"
        val gradleScript = GradleScripting.gradleTemplate.replace(
            "dependencies {",
            "dependencies {  implementation(\"org.slf4j:slf4j-nop:1.7.30\")"
        )
        val urls = GradleScripting.downloadJars(
            projectDir = tempDir,
            taskName = taskName,
            gradleScript = gradleScript,
            setUserFeedback = { }
        )
        val urlFileNames = urls.map {
            it.path.substringAfterLast('/')
        }
        assertThat(urls).hasSize(2)
        assertThat(urlFileNames).contains("slf4j-nop-1.7.30.jar")
        assertThat(urlFileNames).contains("slf4j-api-1.7.30.jar")

        // Check temp files exist
        assertThat(File(tempDir, "gradle_download/slf4j-nop-1.7.30.jar")).exists()
        assertThat(File(tempDir, "gradle_download/slf4j-api-1.7.30.jar")).exists()
        assertThat(File(tempDir, "build.gradle.kts")).exists()
        assertThat(File(tempDir, "settings.gradle.kts")).exists()
        assertThat(File(tempDir, ".gradle")).exists()

        // Clean up temp files
        GradleScripting.cleanUp(projectDir = tempDir)

        // Check temp files are deleted
        assertThat(File(tempDir, "gradle_download")).doesNotExist()
        assertThat(File(tempDir, "build.gradle.kts")).doesNotExist()
        assertThat(File(tempDir, "settings.gradle.kts")).doesNotExist()
        // We want to keep the gradle folder as it takes long to download and can be reused
        assertThat(File(tempDir, ".gradle")).exists()
    }
}
