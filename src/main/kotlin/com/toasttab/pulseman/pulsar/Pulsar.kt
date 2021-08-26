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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader.addJarsToClassLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarAuthHandler
import com.toasttab.pulseman.state.PulsarSettings
import org.apache.pulsar.client.api.Authentication
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.EncodedAuthenticationParameterSupport
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.MessageRoutingMode
import org.apache.pulsar.client.api.Producer
import org.apache.pulsar.client.api.PulsarClient
import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.client.api.SubscriptionType
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Handles
 * - Sending messages on a Pulsar Topic
 * - Monitoring messages on a Pulsar Topic
 */
class Pulsar(
    private val pulsarSettings: PulsarSettings,
    private val setUserFeedback: (String) -> Unit
) {
    companion object {
        private val mapper = ObjectMapper().registerModule(KotlinModule())
        private val mapTypeRef = object : TypeReference<Map<String, String>>() {}
    }

    fun close() {
        try {
            pulsarClient?.shutdown()
        } catch (ex: Throwable) {
            setUserFeedback("Failed to close pulsar:$ex")
        }
    }

    private fun authenticatedPulsarClient(pulsarAuthClass: PulsarAuthHandler): PulsarClient {
        val authHandler = RunTimeJarLoader
            .loader
            .loadClass(pulsarAuthClass.cls.canonicalName)
            .getDeclaredConstructor()
            .newInstance() as Authentication

        (authHandler as EncodedAuthenticationParameterSupport)
            .configure(pulsarSettings.authSelector.authJsonParameters())

        return PulsarClient.builder()
            .serviceUrl(pulsarSettings.serviceUrl.value)
            .operationTimeout(15, TimeUnit.SECONDS)
            .connectionTimeout(15, TimeUnit.SECONDS)
            .authentication(authHandler)
            .build()
    }

    private fun unAuthenticatedPulsarClient() = PulsarClient.builder()
        .serviceUrl(pulsarSettings.serviceUrl.value)
        .operationTimeout(15, TimeUnit.SECONDS)
        .connectionTimeout(15, TimeUnit.SECONDS)
        .build()

    private val pulsarClient by lazy {
        try {
            addJarsToClassLoader()
            pulsarSettings.authSelector.selectedAuthClass.selected?.let { pulsarAuthClass ->
                authenticatedPulsarClient(pulsarAuthClass)
            } ?: unAuthenticatedPulsarClient()
        } catch (ex: Throwable) {
            setUserFeedback("Failed to setup pulsar:$ex")
            null
        }
    }

    private fun createNewProducer(topic: String): Producer<ByteArray>? {
        return try {
            pulsarClient?.newProducer(Schema.BYTES)
                ?.messageRoutingMode(MessageRoutingMode.SinglePartition)
                ?.topic(topic)
                ?.enableBatching(false)
                ?.sendTimeout(500, TimeUnit.MILLISECONDS)
                ?.create()
        } catch (ex: Throwable) {
            setUserFeedback("Failed to create producer:$ex")
            null
        }
    }

    fun createNewConsumer(handleMessage: (Message<ByteArray>) -> Unit): CompletableFuture<Consumer<ByteArray>>? {
        return try {
            pulsarClient?.newConsumer()
                ?.consumerName("pulseman-subscription-${UUID.randomUUID()}")
                ?.topic(pulsarSettings.topic.value)
                ?.subscriptionType(SubscriptionType.Exclusive)
                ?.messageListener { c, m ->
                    c.acknowledge(m)
                    handleMessage(m)
                }
                ?.subscriptionName("pulseman-subscription-${UUID.randomUUID()}")
                ?.subscribeAsync()
        } catch (ex: Throwable) {
            setUserFeedback("Failed to create consumer:$ex")
            null
        }
    }

    private fun properties(): Map<String, String> {
        val propertiesJsonMap = pulsarSettings.propertySettings.propertyMap()
        if (propertiesJsonMap.isNotBlank()) {
            try {
                return mapper.readValue(propertiesJsonMap, mapTypeRef)
            } catch (ex: Exception) {
                setUserFeedback("Failed to deserialize properties=$propertiesJsonMap. Error=$ex")
            }
        }
        return emptyMap()
    }

    fun sendMessage(message: ByteArray?) {
        if (pulsarSettings.serviceUrl.value.isBlank()) {
            setUserFeedback("Service Url is not set")
            return
        }
        val topic = pulsarSettings.topic.value
        if (topic.isBlank()) {
            setUserFeedback("Topic is not set")
            return
        }
        if (message == null) {
            setUserFeedback("No class generated to send")
            return
        }

        return try {
            val messageId = createNewProducer(topic)
                ?.newMessage()
                ?.value(message)
                ?.eventTime(System.currentTimeMillis())
                ?.properties(properties())
                ?.send()

            setUserFeedback("Message sent with id $messageId on topic $topic")
        } catch (ex: Throwable) {
            setUserFeedback("Could not transmit on topic $topic.\nError:$ex")
        }
    }
}
