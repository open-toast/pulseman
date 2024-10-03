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
import com.toasttab.pulseman.AppStrings.COULD_NOT_TRANSMIT_TOPIC
import com.toasttab.pulseman.AppStrings.CREATING_NEW_PRODUCER
import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.AppStrings.FAILED_TO_CLOSE_PULSAR
import com.toasttab.pulseman.AppStrings.FAILED_TO_CREATE_CONSUMER
import com.toasttab.pulseman.AppStrings.FAILED_TO_CREATE_PRODUCER
import com.toasttab.pulseman.AppStrings.FAILED_TO_DESERIALIZE_PROPERTIES
import com.toasttab.pulseman.AppStrings.FAILED_TO_SETUP_PULSAR
import com.toasttab.pulseman.AppStrings.MESSAGE_SENT_ID
import com.toasttab.pulseman.AppStrings.NO_CLASS_GENERATED_TO_SEND
import com.toasttab.pulseman.AppStrings.ON_TOPIC
import com.toasttab.pulseman.AppStrings.SERVICE_URL_NOT_SET
import com.toasttab.pulseman.AppStrings.TOPIC_NOT_SET
import com.toasttab.pulseman.jars.RunTimeJarLoader.addJarsToClassLoader
import com.toasttab.pulseman.state.PulsarSettings
import org.apache.pulsar.client.api.Authentication
import org.apache.pulsar.client.api.Consumer
import org.apache.pulsar.client.api.Message
import org.apache.pulsar.client.api.MessageRoutingMode
import org.apache.pulsar.client.api.Producer
import org.apache.pulsar.client.api.PulsarClient
import org.apache.pulsar.client.api.Schema
import org.apache.pulsar.client.api.SubscriptionMode
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
    private var producer: Producer<ByteArray>? = null
    private val pulsarAuth = PulsarAuth(pulsarSettings)

    fun close() {
        try {
            pulsarClient?.shutdown()
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_CLOSE_PULSAR:$ex")
        }
    }

    private fun authenticatedPulsarClient(authHandler: Authentication): PulsarClient {
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
            pulsarAuth.getAuthHandler()?.let { authHandler ->
                authenticatedPulsarClient(authHandler)
            } ?: unAuthenticatedPulsarClient()
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_SETUP_PULSAR:$ex")
            null
        }
    }

    private fun createNewProducer(topic: String): Producer<ByteArray>? {
        return try {
            setUserFeedback(CREATING_NEW_PRODUCER)
            pulsarClient?.newProducer(Schema.BYTES)
                ?.messageRoutingMode(MessageRoutingMode.SinglePartition)
                ?.topic(topic)
                ?.enableBatching(false)
                ?.sendTimeout(SEND_TIMEOUT, TimeUnit.MILLISECONDS)
                ?.create().also {
                    producer = it
                }
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_CREATE_PRODUCER:$ex")
            null
        }
    }

    fun createNewConsumer(handleMessage: (Message<ByteArray>) -> Unit): CompletableFuture<Consumer<ByteArray>>? {
        return try {
            pulsarClient?.newConsumer()
                ?.consumerName("$SUBSCRIPTION_NAME${UUID.randomUUID()}")
                ?.topic(pulsarSettings.topic.value)
                ?.subscriptionType(SubscriptionType.Exclusive)
                ?.subscriptionMode(SubscriptionMode.NonDurable)
                ?.messageListener { c, m ->
                    c.acknowledge(m)
                    handleMessage(m)
                }
                ?.subscriptionName("$SUBSCRIPTION_NAME${UUID.randomUUID()}")
                ?.subscribeAsync()
        } catch (ex: Throwable) {
            setUserFeedback("$FAILED_TO_CREATE_CONSUMER:$ex")
            null
        }
    }

    private fun properties(): Map<String, String> {
        val propertiesJsonMap = pulsarSettings.propertySettings.propertyMap()
        if (propertiesJsonMap.isNotBlank()) {
            try {
                return mapper.readValue(propertiesJsonMap, mapTypeRef)
            } catch (ex: Exception) {
                setUserFeedback("$FAILED_TO_DESERIALIZE_PROPERTIES=$propertiesJsonMap. $EXCEPTION=$ex")
            }
        }
        return emptyMap()
    }

    fun sendMessage(message: ByteArray?): Boolean {
        var wrongSettings = false
        if (pulsarSettings.serviceUrl.value.isBlank()) {
            setUserFeedback(SERVICE_URL_NOT_SET)
            wrongSettings = true
        }
        val topic = pulsarSettings.topic.value
        if (topic.isBlank()) {
            setUserFeedback(TOPIC_NOT_SET)
            wrongSettings = true
        }
        if (message == null) {
            setUserFeedback(NO_CLASS_GENERATED_TO_SEND)
            wrongSettings = true
        }
        if (wrongSettings) {
            return false
        }

        try {
            (producer ?: createNewProducer(topic))
                ?.newMessage()
                ?.value(message)
                ?.eventTime(System.currentTimeMillis())
                ?.properties(properties())
                ?.send()
                ?.let { messageId ->
                    setUserFeedback("$MESSAGE_SENT_ID $messageId $ON_TOPIC $topic")
                } ?: return false
            return true
        } catch (ex: Throwable) {
            setUserFeedback("$COULD_NOT_TRANSMIT_TOPIC $topic.\n$EXCEPTION:$ex")
            return false
        }
    }

    companion object {
        private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        private val mapTypeRef = object : TypeReference<Map<String, String>>() {}
        private const val SUBSCRIPTION_NAME = "pulseman-subscription-"
        private const val SEND_TIMEOUT = 5000
    }
}
