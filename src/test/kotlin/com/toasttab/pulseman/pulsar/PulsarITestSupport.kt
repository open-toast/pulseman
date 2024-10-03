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

import org.apache.pulsar.client.admin.PulsarAdmin
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.PulsarClient
import org.apache.pulsar.common.policies.data.TenantInfoImpl
import org.testcontainers.containers.PulsarContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.util.UUID
import java.util.concurrent.CompletableFuture

open class PulsarITestSupport {
    fun sendMessage(bytes: ByteArray, topic: String, properties: Map<String, String>) {
        pulsarClient
            .newProducer()
            .topic(topic)
            .create()
            .newMessage()
            .properties(properties)
            .value(bytes)
            .send()
    }

    fun responseFuture(topic: String): CompletableFuture<Message<ByteArray>> {
        val messageResponseFuture = CompletableFuture<Message<ByteArray>>()
        pulsarClient.newConsumer()
            .topic(topic)
            .messageListener { c, m ->
                c.acknowledge(m)
                c.close()
                messageResponseFuture.complete(m)
            }.subscriptionName("test-response-${UUID.randomUUID()}").subscribe()
        return messageResponseFuture
    }

    companion object {
        private const val pulsarVersion = "3.3.1"
        private const val nameSpacePublic = "/admin/v2/namespaces/public"

        private lateinit var admin: PulsarAdmin
        private lateinit var pulsarClient: PulsarClient
        lateinit var initialTopicList: List<String>

        val pulsarContainer: PulsarContainer = PulsarContainer(
            DockerImageName
                .parse("apachepulsar/pulsar")
                .withTag(pulsarVersion)
        ).waitingFor(
            Wait.forHttp(nameSpacePublic)
                .forStatusCode(200).forPort(PulsarContainer.BROKER_HTTP_PORT)
        )

        fun setUp() {
            pulsarContainer.start()

            if (!this::admin.isInitialized) {
                Thread.sleep(2000)
                pulsarClient = PulsarClient.builder().serviceUrl(pulsarContainer.pulsarBrokerUrl).build()
                admin = PulsarAdmin.builder().serviceHttpUrl(pulsarContainer.httpServiceUrl).build()
                setUpTopics()
            }
        }

        private fun setUpTopics() {
            initialTopicList = (0..3).flatMap { tenantNo ->
                val tenant = "tenant$tenantNo"
                admin.tenants().createTenant(tenant, TenantInfoImpl(emptySet(), setOf("standalone")))
                (0..3).flatMap { namespaceNo ->
                    val namespace = "$tenant/namespace$namespaceNo"
                    admin.namespaces().createNamespace(namespace)
                    (0..3).map { topicNo ->
                        val topic = "persistent://$namespace/topic$topicNo"
                        admin.topics().createNonPartitionedTopic(topic)
                        topic
                    }
                }
            }
        }
    }
}
