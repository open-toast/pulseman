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

package com.toasttab.pulseman.jars

import com.toasttab.pulseman.files.FileManagement
import com.toasttab.pulseman.pulsar.filters.protobuf.GeneratedMessageV3Filter
import com.toasttab.pulseman.pulsar.filters.protobuf.KTMessageFilter
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.state.GlobalFeedback
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages the jars needed to serialize and deserialize messages for each tab
 */
data class TabJarManager(
    private val globalFeedback: GlobalFeedback,
    private val dependentJarLoader: RunTimeJarLoader? = null,
    val jarManagers: MutableMap<UUID, JarManager<PulsarMessageClassInfo>> = mutableMapOf()
) {

    fun add(tabID: UUID, newJarFormat: Boolean): JarManager<PulsarMessageClassInfo> {
        val runTimeJarLoader = RunTimeJarLoader(dependentJarLoader = dependentJarLoader)
        return JarManager(
            loadedClasses = LoadedClasses(
                classFilters = listOf(
                    // Add all the pulsar message formats supported here
                    KTMessageFilter(runTimeJarLoader = runTimeJarLoader),
                    GeneratedMessageV3Filter(runTimeJarLoader = runTimeJarLoader)
                ),
                runTimeJarLoader = runTimeJarLoader
            ),
            jarFolderName = "${MESSAGE_JAR_FOLDER}_tab_${currentTabNumber.getAndIncrement()}",
            globalFeedback = globalFeedback,
            runTimeJarLoader = runTimeJarLoader,
            originalJarFolderName = if (newJarFormat) null else MESSAGE_JAR_FOLDER
        ).also { newJarManager ->
            jarManagers[tabID] = newJarManager
            refresh(printError = true)
        }
    }

    fun deleteAllJars() {
        jarManagers.values.forEach { it.deleteAllJars() }
    }

    fun reset() {
        // Search for folders beginning with MESSAGE_JAR_FOLDER and delete them
        FileManagement.appFolder.listFiles()?.forEach {
            if (it.isDirectory && it.name.startsWith("${MESSAGE_JAR_FOLDER}_")) {
                FileManagement.deleteFile(it)
            }
        }
        jarManagers.clear()
    }

    fun refresh(printError: Boolean) {
        jarManagers.values.forEach { it.refresh(printError = printError) }
    }

    fun remove(tabID: UUID) {
        val jarManager = jarManagers[tabID] ?: return
        jarManagers.remove(tabID)
        FileManagement.deleteFile(file = jarManager.jarFolder)
    }

    fun copyTab(fromTabID: UUID, toTabID: UUID) {
        val fromJarManager = jarManagers[fromTabID] ?: return
        val toJarManager = jarManagers[toTabID] ?: return
        toJarManager.copyJars(jarFiles = fromJarManager.loadedJars, printError = true)
    }

    companion object {
        private const val MESSAGE_JAR_FOLDER = "message_jars"
        private var currentTabNumber = AtomicInteger(0)
    }
}
