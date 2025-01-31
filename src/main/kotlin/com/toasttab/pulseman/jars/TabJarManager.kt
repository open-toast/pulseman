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

/**
 * Manages the jars needed to serialize and deserialize messages for each tab
 */
data class TabJarManager(
    private val globalFeedback: GlobalFeedback,
    private val dependentJarLoader: RunTimeJarLoader? = null,
    val jarManagers: MutableMap<Int, JarManager<PulsarMessageClassInfo>> = mutableMapOf()
) {

    fun add(tabNumber: Int, newJarFormat: Boolean): JarManager<PulsarMessageClassInfo> {
        val runTimeJarLoader = RunTimeJarLoader(dependentJarLoader = dependentJarLoader)
        return JarManager(
            loadedClasses = LoadedClasses(
                classFilters = listOf(
                    // Add all the pulsar message formats supported here
                    KTMessageFilter(runTimeJarLoader = runTimeJarLoader),
                    GeneratedMessageV3Filter(runTimeJarLoader = runTimeJarLoader)
                )
            ),
            jarFolderName = "${MESSAGE_JAR_FOLDER}_tab_$tabNumber",
            globalFeedback = globalFeedback,
            runTimeJarLoader = runTimeJarLoader,
            originalJarFolderName = if (newJarFormat) null else MESSAGE_JAR_FOLDER
        ).also {
            jarManagers[tabNumber] = it
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

    fun remove(tabNumber: Int) {
        jarManagers.remove(tabNumber)
    }

    fun copyTab(fromTabNumber: Int, toTabNumber: Int) {
        val fromJarManager = jarManagers[fromTabNumber] ?: return
        val toJarManager = jarManagers[toTabNumber] ?: return
        toJarManager.copyJars(jarFiles = fromJarManager.loadedJars, printError = true)
    }

    companion object {
        private const val MESSAGE_JAR_FOLDER = "message_jars"
    }
}
