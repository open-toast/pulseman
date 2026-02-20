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

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.AppStrings
import com.toasttab.pulseman.TestMessage
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessageHandlingClassImplTest {

    private lateinit var selectedProtoClass: SingleSelection<PulsarMessageClassInfo>
    private lateinit var receivedMessages: SnapshotStateList<ReceivedMessages>
    private lateinit var userFeedback: MutableList<String>
    private lateinit var propertyFilterMap: Map<String, String>
    private lateinit var messageHandling: MessageHandlingClassImpl
    private lateinit var testProtoClassInfo: TestPulsarMessageClassInfo

    @BeforeEach
    fun setup() {
        selectedProtoClass = SingleSelection()
        receivedMessages = SnapshotStateList()
        userFeedback = mutableListOf()
        propertyFilterMap = emptyMap()
        testProtoClassInfo = TestPulsarMessageClassInfo()

        messageHandling = MessageHandlingClassImpl(
            selectedProtoClass = selectedProtoClass,
            propertyFilter = { propertyFilterMap },
            receivedMessages = receivedMessages,
            setUserFeedback = { userFeedback.add(it) }
        )
    }

    private class TestPulsarMessageClassInfo : PulsarMessageClassInfo {
        var deserializeResult: Any = "Deserialized object"
        var prettyPrintResult: String = "Pretty printed message"
        var shouldThrowOnDeserialize: Boolean = false

        override val cls: Class<out Any> = String::class.java
        override val runTimeJarLoader: RunTimeJarLoader = RunTimeJarLoader()

        override fun serialize(cls: Any): ByteArray = ByteArray(0)

        override fun deserialize(bytes: ByteArray): Any {
            if (shouldThrowOnDeserialize) {
                throw RuntimeException("Deserialization error")
            }
            return deserializeResult
        }

        override fun prettyPrint(cls: Any): String = prettyPrintResult

        override fun generateClassTemplate(): String = ""

        override fun getJarLoader(): JarLoader = throw UnsupportedOperationException()
    }

    @Test
    fun `skippedMessages is zero initially`() {
        assertThat(messageHandling.skippedMessages).isEqualTo(0)
    }

    @Test
    fun `skippedMessages increments when message is skipped`() {
        val message = TestMessage(messageProperties = mapOf("environment" to "staging"))
        propertyFilterMap = mapOf("environment" to "production")

        messageHandling.parseMessage(message)

        assertThat(messageHandling.skippedMessages).isEqualTo(1)
    }

    @Test
    fun `skippedMessages increments for each skipped message`() {
        val message = TestMessage(messageProperties = mapOf("environment" to "staging"))
        propertyFilterMap = mapOf("environment" to "production")

        repeat(3) { messageHandling.parseMessage(message) }

        assertThat(messageHandling.skippedMessages).isEqualTo(3)
    }

    @Test
    fun `skippedMessages does not increment when message is processed`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production"),
            messageData = ByteArray(10)
        )
        propertyFilterMap = mapOf("environment" to "production")
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(messageHandling.skippedMessages).isEqualTo(0)
    }

    @Test
    fun `parseMessage skips message when filter does not match`() {
        val message = TestMessage(messageProperties = mapOf("environment" to "staging"))
        propertyFilterMap = mapOf("environment" to "production")

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).isEmpty()
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `parseMessage processes message when filter matches`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production"),
            messageData = ByteArray(10)
        )
        propertyFilterMap = mapOf("environment" to "production")
        testProtoClassInfo.prettyPrintResult = "Deserialized message content"
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
        assertThat(receivedMessages[0].message).contains("Deserialized message content")
        assertThat(receivedMessages[0].message).contains("environment=production")
        assertThat(receivedMessages[0].header).contains("String")
    }

    @Test
    fun `parseMessage processes message when filter is empty`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production"),
            messageData = ByteArray(10)
        )
        testProtoClassInfo.prettyPrintResult = "Deserialized message content"
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
    }

    @Test
    fun `parseMessage sets user feedback when no class is selected`() {
        val message = TestMessage(messageProperties = emptyMap())
        selectedProtoClass.selected = null

        messageHandling.parseMessage(message)

        assertThat(userFeedback).containsExactly(AppStrings.NO_CLASS_SELECTED_DESERIALIZE)
        assertThat(receivedMessages).isEmpty()
    }

    @Test
    fun `parseMessage sets user feedback when deserialization fails`() {
        val message = TestMessage(messageProperties = emptyMap(), messageData = ByteArray(10))
        testProtoClassInfo.shouldThrowOnDeserialize = true
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(userFeedback).hasSize(1)
        assertThat(userFeedback[0]).contains(AppStrings.FAILED_TO_DESERIALIZE_PULSAR)
        assertThat(receivedMessages).isEmpty()
    }

    @Test
    fun `parseMessage removes oldest message when max messages exceeded`() {
        // Removal triggers when size > MAX_MESSAGES_STORED (500), i.e. on the 502nd insert
        val message = TestMessage(messageProperties = emptyMap(), messageData = ByteArray(10))
        testProtoClassInfo.prettyPrintResult = "content"
        selectedProtoClass.selected = testProtoClassInfo

        repeat(MAX_MESSAGES_STORED + 2) {
            messageHandling.parseMessage(message)
        }

        assertThat(receivedMessages).hasSize(MAX_MESSAGES_STORED + 1)
    }

    @Test
    fun `parseMessage does not evict at exactly max messages stored`() {
        val message = TestMessage(messageProperties = emptyMap(), messageData = ByteArray(10))
        testProtoClassInfo.prettyPrintResult = "content"
        selectedProtoClass.selected = testProtoClassInfo

        repeat(MAX_MESSAGES_STORED) {
            messageHandling.parseMessage(message)
        }

        assertThat(receivedMessages).hasSize(MAX_MESSAGES_STORED)
    }

    @Test
    fun `parseMessage processes message when all multiple filters match`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production", "region" to "us-east-1"),
            messageData = ByteArray(10)
        )
        propertyFilterMap = mapOf("environment" to "production", "region" to "us-east-1")
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
    }

    @Test
    fun `parseMessage processes message when one of multiple filters matches`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production", "region" to "eu-west-1"),
            messageData = ByteArray(10)
        )
        propertyFilterMap = mapOf("environment" to "production", "region" to "us-east-1")
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
    }

    @Test
    fun `parseMessage skips message when none of multiple filters match`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "staging", "region" to "eu-west-1")
        )
        propertyFilterMap = mapOf("environment" to "production", "region" to "us-east-1")

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).isEmpty()
    }

    @Test
    fun `parseMessage skips message when filter key is absent from message properties`() {
        val message = TestMessage(messageProperties = emptyMap())
        propertyFilterMap = mapOf("environment" to "production")

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).isEmpty()
    }

    @Test
    fun `parseMessage processes message when message has extra properties beyond filter`() {
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production", "region" to "us-east-1"),
            messageData = ByteArray(10)
        )
        propertyFilterMap = mapOf("environment" to "production")
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
    }

    @Test
    fun `parseMessage includes message properties in output`() {
        val properties = mapOf("environment" to "production", "version" to "1.0", "region" to "us-east-1")
        val message = TestMessage(messageProperties = properties, messageData = ByteArray(10))
        testProtoClassInfo.prettyPrintResult = "Deserialized message content"
        selectedProtoClass.selected = testProtoClassInfo

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
        val body = receivedMessages[0].message
        assertThat(body).contains("environment=production")
        assertThat(body).contains("version=1.0")
        assertThat(body).contains("region=us-east-1")
    }

    companion object {
        private const val MAX_MESSAGES_STORED = 500
    }
}
