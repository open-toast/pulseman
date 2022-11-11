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

/**
 * Stores the settings for each tab, will be serialized to a json string and saved in the project zip.
 *
 * This is deprecated, keeping it for backwards compatibility with old config format, will eventually delete altogether.
 */
@Deprecated(
    "This is an old save format use TabValuesV3",
    replaceWith = ReplaceWith("TabValuesV3"),
    level = DeprecationLevel.WARNING
)
data class TabValuesV1(
    val tabName: String?,
    val topic: String?,
    val serviceUrl: String?,
    val code: String?,
    val selectedClassSend: String?,
    val selectedClassReceive: List<String>,
    val selectedAuthClass: String?,
    val authJsonParameters: String?,
    val propertyMap: String?
)
