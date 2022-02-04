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

package com.toasttab.pulseman

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.toasttab.pulseman.entities.ClassInfo
import com.toasttab.pulseman.entities.ProjectSettings
import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.jars.JarManager
import com.toasttab.pulseman.jars.LoadedClasses
import com.toasttab.pulseman.pulsar.filters.AuthClassFilter
import com.toasttab.pulseman.pulsar.filters.GeneratedMessageV3Filter
import com.toasttab.pulseman.pulsar.filters.KTMessageFilter
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import com.toasttab.pulseman.state.TabHolder

class AppState {
    val pulsarMessageJars: JarManager<PulsarMessage> = JarManager(
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
                    mapper.readValue(loadedProject, ProjectSettings::class.java).tabs
                }
            if (newTabs != null) {
                jarManagers.forEach { it.refresh() }
                requestTabs.closeAll()
                requestTabs.load(newTabs)
            }
        }
    }

    fun loadDefault(initialMessage: String?) {
        requestTabs.open(initialMessage)
    }

    fun save(quickSave: Boolean) {
        FileManagement.saveProject(
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ProjectSettings(requestTabs.allTabValues())),
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
