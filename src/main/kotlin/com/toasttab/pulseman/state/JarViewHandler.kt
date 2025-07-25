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

package com.toasttab.pulseman.state

import com.toasttab.pulseman.entities.ClassInfo

/**
 * A sealed class that represents the different views that can be displayed in the JarManagementTabs.
 */
sealed class JarViewHandler(open val title: String) {
    class JarManagementView(val jarManagement: Pair<String, JarManagement<out ClassInfo>>) :
        JarViewHandler(jarManagement.first)

    class GradleManagementView(val gradleManagement: Pair<String, GradleManagement>) :
        JarViewHandler(gradleManagement.first)
}
