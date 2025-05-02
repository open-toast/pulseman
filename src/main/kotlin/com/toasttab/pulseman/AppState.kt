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
@file:Suppress("DEPRECATION")

package com.toasttab.pulseman

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.toasttab.pulseman.AppStrings.PROJECT_LOAD
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.ProjectSettingsV1
import com.toasttab.pulseman.entities.ProjectSettingsV2
import com.toasttab.pulseman.entities.ProjectSettingsV3
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.jars.LoadedClasses
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.jars.TabJarManager
import com.toasttab.pulseman.pulsar.filters.AuthClassFilter
import com.toasttab.pulseman.pulsar.filters.protobuf.GeneratedMessageV3Filter
import com.toasttab.pulseman.pulsar.filters.protobuf.KTMessageFilter
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.state.GlobalFeedback
import com.toasttab.pulseman.state.GradleManagement
import com.toasttab.pulseman.state.TabHolder

class AppState {
    val fileManagement: FileManagement = FileManagement()

    val javaHome: MutableState<String> = mutableStateOf("")

    val globalFeedback = GlobalFeedback()

    // A common Jar Loader, any jars loaded here will be available to all tabs and auth functionality
    private val commonJarLoader = RunTimeJarLoader()

    // This is used for auth operation, inherits the commonJarLoader and will be available in all tabs
    private val authJarLoader = RunTimeJarLoader(dependentJarLoader = commonJarLoader)

    // A jar loader for storing global pulsar message jars, store messages jars here to make them available to all tabs
    private val messageJarLoader = RunTimeJarLoader(dependentJarLoader = authJarLoader)

    val commonJars: JarManager<ClassInfo> = JarManager(
        loadedClasses = LoadedClasses(
            classFilters = emptyList(),
            runTimeJarLoader = commonJarLoader
        ),
        jarFolderName = DEPENDENCY_JAR_FOLDER,
        globalFeedback = globalFeedback,
        runTimeJarLoader = commonJarLoader,
        originalJarFolderName = null,
        tabFileExtension = null,
        fileManagement = fileManagement
    )

    val messageJars: JarManager<PulsarMessageClassInfo> = JarManager(
        loadedClasses = LoadedClasses(
            classFilters = listOf(
                GeneratedMessageV3Filter(runTimeJarLoader = messageJarLoader),
                KTMessageFilter(runTimeJarLoader = messageJarLoader)
            ),
            runTimeJarLoader = messageJarLoader
        ),
        jarFolderName = GLOBAL_MESSAGE_JAR_FOLDER,
        globalFeedback = globalFeedback,
        runTimeJarLoader = messageJarLoader,
        originalJarFolderName = null,
        tabFileExtension = null,
        fileManagement = fileManagement
    )

    val authJars: JarManager<PulsarAuthHandler> = JarManager(
        loadedClasses = LoadedClasses(
            classFilters = listOf(
                // Add all the pulsar auth classes supported here
                AuthClassFilter()
            ),
            runTimeJarLoader = authJarLoader
        ),
        jarFolderName = AUTH_JAR_FOLDER,
        globalFeedback = globalFeedback,
        runTimeJarLoader = authJarLoader,
        originalJarFolderName = null,
        tabFileExtension = null,
        fileManagement = fileManagement
    )

    val tabJarManager = TabJarManager(
        globalFeedback = globalFeedback,
        dependentJarLoader = messageJarLoader,
        fileManagement = fileManagement
    )

    private val jarManagers = listOf(authJars, commonJars, messageJars)

    private val gradleManagement = GradleManagement(
        setUserFeedback = globalFeedback::set,
        commonJarManager = commonJars,
        pulsarJarManagers = listOf(messageJars, authJars),
        onChange = { }, // TODO make it possible to notify about global state changes
        taskPrefix = GLOBAL_GRADLE_TASK_NAME,
        gradleScript = null, // Is loaded by loadFile below
        javaHome = javaHome,
        fileManagement = fileManagement
    )

    val requestTabs = TabHolder(appState = this, globalGradleManagement = gradleManagement)

    fun loadFile(loadDefault: Boolean) {
        globalFeedback.reset()
        fileManagement.getProjectFile(loadDefault)?.let { projectFile ->
            jarManagers.forEach { it.deleteAllJars() }
            tabJarManager.deleteAllJars()
            tabJarManager.reset()
            val projectSettings =
                fileManagement.loadProject(file = projectFile, setUserFeedback = globalFeedback::set)
                    ?.let { loadedProject ->
                        loadConfig(project = loadedProject)
                    }
            if (projectSettings?.tabs != null) {
                jarManagers.forEach {
                    it.refresh(printError = true)
                }
                tabJarManager.refresh(printError = true)
                requestTabs.closeAll()
                requestTabs.load(
                    tabSettings = projectSettings.tabs,
                    newJarFormat = projectSettings.newJarFormatUsed ?: false
                )
            }
            projectSettings?.gradleScript?.let {
                gradleManagement.loadGradleScript(it)
            } ?: gradleManagement.generateGradleTemplate()

            projectSettings?.javaHome?.let {
                javaHome.value = it
            } ?: run {
                javaHome.value = ""
            }
        }
    }

    private fun loadConfig(project: String): ProjectSettingsV3 {
        val error = StringBuilder()
        try {
            return mapper.readValue(project, ProjectSettingsV3::class.java)
        } catch (ex: Exception) {
            error.appendLine("V3 $PROJECT_LOAD:$ex")
        }
        try {
            return mapper.readValue(project, ProjectSettingsV2::class.java).toV3()
        } catch (ex: Exception) {
            error.appendLine("V2 $PROJECT_LOAD:$ex")
        }
        try {
            return mapper.readValue(project, ProjectSettingsV1::class.java).toV3()
        } catch (ex: Exception) {
            error.append("V1 $PROJECT_LOAD:$ex")
        }

        throw Exception(error.toString())
    }

    fun loadDefault(initialMessage: String?) {
        requestTabs.open(initialMessage)
    }

    fun save(quickSave: Boolean) {
        fileManagement.saveProject(
            tabsJson = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(
                    ProjectSettingsV3(
                        configVersion = ProjectSettingsV3.CURRENT_VERSION,
                        gradleScript = gradleManagement.currentGradleScript(),
                        javaHome = javaHome.value,
                        tabs = requestTabs.allTabValues(),
                        newJarFormatUsed = true
                    )
                ),
            quickSave = quickSave,
            jarFolders = tabJarManager.jarManagers.map { it.value.jarFolder }
                .plus(jarManagers.map { it.jarFolder })
        )
        requestTabs.savedChanges()
    }

    fun copyCurrentTab() {
        requestTabs.copyCurrentTab()
    }

    private val mapper = ObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        registerModule(KotlinModule.Builder().build())
    }

    companion object {
        private const val AUTH_JAR_FOLDER = "auth_jars"
        private const val DEPENDENCY_JAR_FOLDER = "dependency_jars"
        private const val GLOBAL_MESSAGE_JAR_FOLDER = "global_message_jars"
        private const val GLOBAL_GRADLE_TASK_NAME = "global"
    }
}
