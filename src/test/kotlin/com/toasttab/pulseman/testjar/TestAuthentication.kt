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

package com.toasttab.pulseman.testjar

import org.apache.pulsar.client.api.Authentication

/**
 * Do not delete, this class is used to generate a JAR used in the LoadedClassesTest tests
 *
 * The createTestJar gradle task creates the JAR file.
 */
class TestAuthentication : Authentication {
    override fun close() {
        TODO("Not yet implemented")
    }

    override fun getAuthMethodName(): String {
        TODO("Not yet implemented")
    }

    @Deprecated("", ReplaceWith(""))
    override fun configure(authParams: MutableMap<String, String>?) {
        TODO("Not yet implemented")
    }

    override fun start() {
        TODO("Not yet implemented")
    }
}
