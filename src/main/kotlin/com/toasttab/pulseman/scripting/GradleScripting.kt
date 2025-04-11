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

import com.toasttab.pulseman.AppStrings.GRADLE_ALREADY_RUNNING
import com.toasttab.pulseman.AppStrings.GRADLE_ERROR
import com.toasttab.pulseman.AppStrings.JAVA_HOME_ERROR
import com.toasttab.pulseman.AppStrings.RUNNING_GRADLE_TASK
import com.toasttab.pulseman.AppStrings.SETTING_UP_GRADLE
import com.toasttab.pulseman.AppStrings.SETUP
import kotlinx.coroutines.sync.Mutex
import org.gradle.tooling.GradleConnector
import java.io.File
import java.net.URL

/**
 * Handles downloading project jars through Gradle instead of having to manually add them to the project
 */
object GradleScripting {
    private val mutex = Mutex()
    private const val SETTINGS_FILE_NAME = "settings.gradle.kts"
    private const val BUILD_FILE_NAME = "build.gradle.kts"
    private const val GRADLE_PROPERTIES_FILE_NAME = "gradle.properties"
    private const val SETTINGS_FILE_CONTENT = "rootProject.name = \"pulseman\""
    private const val COPY_JAR_FOLDER = "gradle_download"
    private const val JAR_EXTENSION = ".jar"
    private const val JAVA_HOME_SETTING = "org.gradle.java.home="
    private val javaHome = System.getenv("JAVA_HOME")

    private fun createGradleBuildFiles(
        projectDir: File,
        buildFileContent: String,
        setUserFeedback: (String) -> Unit
    ) {
        val settingsFile = File(projectDir, SETTINGS_FILE_NAME)
        val buildFile = File(projectDir, BUILD_FILE_NAME)
        val gradlePropertiesFile = File(projectDir, GRADLE_PROPERTIES_FILE_NAME)
        settingsFile.writeText(SETTINGS_FILE_CONTENT)
        buildFile.writeText(buildFileContent)
        javaHome?.let {
            gradlePropertiesFile.writeText("$JAVA_HOME_SETTING$javaHome")
        } ?: run {
            setUserFeedback("${JAVA_HOME_ERROR}null")
        }
    }

    private fun runGradleTask(
        projectDir: File,
        taskName: String,
        setUserFeedback: (String) -> Unit
    ) {
        val output = StringBuilder()
        val outputHandler = object : java.io.OutputStream() {
            override fun write(b: Int) {
                output.append(b.toChar())
            }
        }

        try {
            val connector = GradleConnector.newConnector().forProjectDirectory(projectDir)
            connector.connect().use { connection ->
                connection.newBuild().apply {
                    forTasks(taskName)
                    setStandardOutput(outputHandler)
                    setStandardError(outputHandler)
                }.run()
            }
        } catch (ex: Exception) {
            setUserFeedback("$GRADLE_ERROR $ex")
            setUserFeedback("$JAVA_HOME_ERROR:$javaHome")
            setUserFeedback(output.toString())
        }
        setUserFeedback(output.toString())
    }

    private fun getDownloadedURLs(projectDir: File): Set<URL> {
        return File(projectDir, COPY_JAR_FOLDER).listFiles()?.filter {
            it.name.endsWith(JAR_EXTENSION)
        }?.map {
            it.toURI().toURL()
        }?.toSet() ?: emptySet()
    }

    private fun getBaseUrls(
        projectDir: File,
        taskName: String,
        setUserFeedback: (String) -> Unit
    ): Set<URL> {
        createGradleBuildFiles(
            projectDir = projectDir,
            buildFileContent = buildFileContent(gradleScript = gradleTemplate, taskName = taskName),
            setUserFeedback = setUserFeedback
        )
        setUserFeedback(SETTING_UP_GRADLE)
        runGradleTask(projectDir = projectDir, taskName = taskName, setUserFeedback = setUserFeedback)
        return getDownloadedURLs(projectDir)
    }

    private fun downloadJarsInternal(
        projectDir: File,
        taskName: String,
        gradleScript: String,
        setUserFeedback: (String) -> Unit
    ): List<URL> {
        val baseURls = getBaseUrls(
            projectDir = projectDir,
            taskName = "${taskName}_$SETUP",
            setUserFeedback = setUserFeedback
        )
        createGradleBuildFiles(
            projectDir = projectDir,
            buildFileContent = buildFileContent(gradleScript = gradleScript, taskName = taskName),
            setUserFeedback = setUserFeedback
        )
        setUserFeedback("$RUNNING_GRADLE_TASK: $taskName")
        runGradleTask(projectDir = projectDir, taskName = taskName, setUserFeedback = setUserFeedback)
        val allUrls = getDownloadedURLs(projectDir)
        val downloadedUrls = allUrls.minus(baseURls).toList()
        return downloadedUrls
    }

    fun downloadJars(
        projectDir: File,
        taskName: String,
        gradleScript: String,
        setUserFeedback: (String) -> Unit
    ): List<URL> {
        if (mutex.tryLock()) {
            try {
                return downloadJarsInternal(projectDir, taskName, gradleScript, setUserFeedback)
            } finally {
                mutex.unlock()
            }
        } else {
            throw Exception("$taskName $GRADLE_ALREADY_RUNNING")
        }
    }

    private fun cleanUpInternal(projectDir: File) {
        File(projectDir, SETTINGS_FILE_NAME).delete()
        File(projectDir, BUILD_FILE_NAME).delete()
        File(projectDir, GRADLE_PROPERTIES_FILE_NAME).delete()
        File(projectDir, COPY_JAR_FOLDER).deleteRecursively()
    }

    fun cleanUp(projectDir: File) {
        if (mutex.tryLock()) {
            try {
                cleanUpInternal(projectDir)
            } finally {
                mutex.unlock()
            }
        } else {
            throw Exception(GRADLE_ALREADY_RUNNING)
        }
    }

    private fun buildFileContent(gradleScript: String, taskName: String) =
        "$gradleScript\n${generateGradleCopyTask(taskName = taskName)}"

    private fun generateGradleCopyTask(taskName: String) = """
            tasks.register("$taskName") {
                doLast {
                    copy {
                        from(configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") })
                        into("gradle_download")
                    }
                }
            }
    """.trimIndent()

    val gradleTemplate = """
            plugins {
                kotlin("jvm") version "2.0.21"
            }

            repositories {
                mavenCentral()
            }

            dependencies {
            }
    """.trimIndent()
}
