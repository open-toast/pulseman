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

import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.AppStrings.FAILED_TO_RETRIEVE_TOPICS
import org.apache.pulsar.client.admin.PulsarAdmin
import java.util.concurrent.TimeUnit

/**
 * Handles connecting to pulsar admin and retrieving a list of topics, this is currently limited to unauthenticated
 * connections
 *
 * TODO make this work with auth
 */
class PulsarConfig(private val setUserFeedback: (String) -> Unit) {
    fun getTopics(pulsarUrl: String): List<String> {
        try {
            PulsarAdmin.builder()
                .serviceHttpUrl(pulsarUrl)
                .connectionTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .requestTimeout(30, TimeUnit.SECONDS)
                .build().use { admin ->
                    return admin.tenants().tenants.flatMap { tenant ->
                        admin.namespaces().getNamespaces(tenant).flatMap { namespace ->
                            admin.topics().getList(namespace).map { it }
                        }
                    }
                }
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_RETRIEVE_TOPICS. $EXCEPTION:$ex")
            return emptyList()
        }
    }
}
