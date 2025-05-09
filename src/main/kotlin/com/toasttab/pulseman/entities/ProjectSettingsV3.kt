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

package com.toasttab.pulseman.entities

data class ProjectSettingsV3(
    val configVersion: String,
    val gradleScript: String?,
    val javaHome: String?,
    val newJarFormatUsed: Boolean?, // This is a nullable field to support older versions of the project settings
    val tabs: List<TabValuesV3>
) : ProjectSettings {

    override fun toV3() = this

    companion object {
        const val CURRENT_VERSION = "3.0.0"
    }
}
