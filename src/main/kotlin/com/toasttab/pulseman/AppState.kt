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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.ProjectSettingsV1
import com.toasttab.pulseman.entities.ProjectSettingsV2
import com.toasttab.pulseman.entities.ProjectSettingsV3
import com.toasttab.pulseman.entities.TabValuesV3
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.jars.LoadedClasses
import com.toasttab.pulseman.pulsar.filters.AuthClassFilter
import com.toasttab.pulseman.pulsar.filters.protobuf.GeneratedMessageV3Filter
import com.toasttab.pulseman.pulsar.filters.protobuf.KTMessageFilter
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.state.TabHolder

class AppState {
    val pulsarMessageJars: JarManager<PulsarMessageClassInfo> = JarManager(
        loadedClasses = LoadedClasses(
            classFilters = listOf(
                // Add all the pulsar message formats supported here
                KTMessageFilter(),
                GeneratedMessageV3Filter()
            )
        ),
        jarFolderName = MESSAGE_JAR_FOLDER
    )

    val authJars: JarManager<PulsarAuthHandler> = JarManager(
        loadedClasses = LoadedClasses(
            classFilters = listOf(
                // Add all the pulsar auth classes supported here
                AuthClassFilter()
            )
        ),
        jarFolderName = AUTH_JAR_FOLDER
    )

    val dependencyJars: JarManager<ClassInfo> = JarManager(
        loadedClasses = LoadedClasses(
            classFilters = emptyList()
        ),
        jarFolderName = DEPENDENCY_JAR_FOLDER
    )

    private val jarManagers = listOf(pulsarMessageJars, authJars, dependencyJars)

    val requestTabs = TabHolder(this)

    fun loadFile(loadDefault: Boolean) {
        FileManagement.getProjectFile(loadDefault)?.let { projectFile ->
            jarManagers.forEach { it.deleteAllJars() }
            val newTabs =
                FileManagement.loadProject(projectFile)?.let { loadedProject ->
                    loadConfig(loadedProject)
                }
            if (newTabs != null) {
                jarManagers.forEach { it.refresh() }
                requestTabs.closeAll()
                requestTabs.load(newTabs)
            }
        }
    }

    private fun loadConfig(project: String): List<TabValuesV3> {
        val error = StringBuilder()
        try {
            return mapper.readValue(project, ProjectSettingsV3::class.java).toV3()
        } catch (ex: Exception) {
            error.appendLine("V3 project load:$ex")
        }
        try {
            return mapper.readValue(project, ProjectSettingsV2::class.java).toV3()
        } catch (ex: Exception) {
            error.appendLine("V2 project load:$ex")
        }
        try {
            return mapper.readValue(project, ProjectSettingsV1::class.java).toV3()
        } catch (ex: Exception) {
            error.append("V1 project load:$ex")
        }

        throw Exception(error.toString())
    }

    fun loadDefault(initialMessage: String?) {
        requestTabs.open(initialMessage)
    }

    fun save(quickSave: Boolean) {
        FileManagement.saveProject(
            mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(
                    ProjectSettingsV3(
                        configVersion = ProjectSettingsV3.currentVersion,
                        tabs = requestTabs.allTabValues()
                    )
                ),
            quickSave,
            jarManagers.map { it.jarFolder }
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
        private const val MESSAGE_JAR_FOLDER = "message_jars"
    }
}
