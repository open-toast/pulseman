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

package com.toasttab.pulseman.pulsar

import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.state.PulsarSettings
import org.apache.pulsar.client.api.Authentication
import org.apache.pulsar.client.api.EncodedAuthenticationParameterSupport

/**
 * Handles creating an authorization provider for pulsar connection
 *
 * If your pulsar set up utilizes authentication you can import your own Auth classes if they implement these apache
 * pulsar interfaces.
 * Authentication - https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/Authentication.html)
 * EncodedAuthenticationParameterSupport - https://pulsar.apache.org/api/client/org/apache/pulsar/client/api/EncodedAuthenticationParameterSupport.html
 *
 * You will then need to provide your auth settings as a string, these will be passed to the **configure** method of the
 * EncodedAuthenticationParameterSupport interface at runtime.
 */
class PulsarAuth(private val pulsarSettings: PulsarSettings) {
    fun getAuthHandler(): Authentication? {
        val pulsarAuthClass = pulsarSettings.authSelector.selectedAuthClass.selected
            ?: return null

        val authHandler = RunTimeJarLoader
            .loader
            .loadClass(pulsarAuthClass.cls.canonicalName)
            .getDeclaredConstructor()
            .newInstance() as Authentication

        (authHandler as EncodedAuthenticationParameterSupport)
            .configure(pulsarSettings.authSelector.authJsonParameters())

        return authHandler
    }
}
