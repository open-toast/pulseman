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
import com.toasttab.pulseman.TestPulsarMessage
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MessageHandlingImplTest {

    private lateinit var messageType: SingleSelection<PulsarMessage>
    private lateinit var receivedMessages: SnapshotStateList<ReceivedMessages>
    private lateinit var userFeedback: MutableList<String>
    private lateinit var propertyFilterMap: Map<String, String>
    private lateinit var messageHandling: MessageHandlingImpl
    private lateinit var message: TestMessage
    private lateinit var testPulsarMessage: TestPulsarMessage

    @BeforeEach
    fun setup() {
        messageType = SingleSelection()
        receivedMessages = SnapshotStateList()
        userFeedback = mutableListOf()
        propertyFilterMap = emptyMap()
        testPulsarMessage = TestPulsarMessage()
        message = TestMessage()
        messageHandling = MessageHandlingImpl(
            messageType = messageType,
            propertyFilter = { propertyFilterMap },
            receivedMessages = receivedMessages,
            setUserFeedback = { userFeedback.add(it) }
        )
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
        val message = TestMessage(messageProperties = emptyMap(), messageData = ByteArray(10))
        testPulsarMessage.deserializeResult = "content"
        messageType.selected = testPulsarMessage

        messageHandling.parseMessage(message)

        assertThat(messageHandling.skippedMessages).isEqualTo(0)
    }

    @Test
    fun `parseMessage skips message when filter does not match`() {
        propertyFilterMap = mapOf("environment" to "production") // message has "environment" to "test"

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).isEmpty()
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `parseMessage processes message when filter matches`() {
        val deserializedData = "Deserialized text message content"
        testPulsarMessage.deserializeResult = deserializedData

        messageType.selected = testPulsarMessage
        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
        assertThat(receivedMessages[0].message).contains(deserializedData)
        assertThat(receivedMessages[0].message).contains("environment=test")
    }

    @Test
    fun `parseMessage processes message when filter is empty`() {
        val messageData = ByteArray(10)
        val deserializedData = "Text message content"
        val message = TestMessage(
            messageProperties = mapOf("environment" to "production"),
            messageData = messageData,
            messagePublishTime = 1234567890000L
        )
        propertyFilterMap = emptyMap()
        testPulsarMessage.deserializeResult = deserializedData

        messageType.selected = testPulsarMessage

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
    }

    @Test
    fun `parseMessage handles null message type gracefully`() {
        val messageData = ByteArray(10)
        val message = TestMessage(
            messageProperties = emptyMap(),
            messageData = messageData,
            messagePublishTime = 1234567890000L
        )
        propertyFilterMap = emptyMap()

        messageType.selected = null

        messageHandling.parseMessage(message)

        // Should add a message with null content (Elvis operator returns null)
        assertThat(receivedMessages).hasSize(1)
        assertThat(receivedMessages[0].message).startsWith("null")
    }

    @Test
    fun `parseMessage sets user feedback when deserialization fails`() {
        val messageData = ByteArray(10)
        val message = TestMessage(
            messageProperties = emptyMap(),
            messageData = messageData
        )
        propertyFilterMap = emptyMap()
        testPulsarMessage.shouldThrowOnDeserialize = true

        messageType.selected = testPulsarMessage

        messageHandling.parseMessage(message)

        assertThat(userFeedback).hasSize(1)
        assertThat(userFeedback[0]).contains(AppStrings.FAILED_TO_DESERIALIZE_PULSAR)
    }

    @Test
    fun `parseMessage includes message properties in output`() {
        val messageData = ByteArray(10)
        val deserializedData = "Text message content"
        val properties = mapOf(
            "environment" to "production",
            "version" to "1.0",
            "service" to "order-service"
        )
        val message = TestMessage(
            messageProperties = properties,
            messageData = messageData,
            messagePublishTime = 1234567890000L
        )
        propertyFilterMap = emptyMap()
        testPulsarMessage.deserializeResult = deserializedData

        messageType.selected = testPulsarMessage

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
        val receivedMessage = receivedMessages[0].message
        assertThat(receivedMessage).contains("environment=production")
        assertThat(receivedMessage).contains("version=1.0")
        assertThat(receivedMessage).contains("service=order-service")
    }

    @Test
    fun `parseMessage adds messages sequentially without removing old ones`() {
        val message = TestMessage(messageProperties = emptyMap(), messageData = ByteArray(10))
        testPulsarMessage.deserializeResult = "Message content"
        messageType.selected = testPulsarMessage

        // Use a count above MessageHandlingClassImpl's eviction threshold (500) to confirm
        // there is no eviction in MessageHandlingImpl
        val count = MAX_MESSAGES_STORED + 2
        repeat(count) {
            messageHandling.parseMessage(message)
        }

        assertThat(receivedMessages).hasSize(count)
    }

    @Test
    fun `parseMessage skips when multiple filter properties dont match`() {
        val message = TestMessage(
            messageProperties = mapOf(
                "environment" to "staging",
                "version" to "1.0"
            )
        )
        propertyFilterMap = mapOf(
            "environment" to "production",
            "version" to "2.0"
        )

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).isEmpty()
    }

    @Test
    fun `parseMessage processes when all multiple filter properties match`() {
        val messageData = ByteArray(10)
        val message = TestMessage(
            messageProperties = mapOf(
                "environment" to "production",
                "version" to "1.0",
                "region" to "us-east-1"
            ),
            messageData = messageData,
            messagePublishTime = 1234567890000L
        )
        propertyFilterMap = mapOf(
            "environment" to "production",
            "version" to "1.0"
        )
        testPulsarMessage.deserializeResult = "Message content"

        messageType.selected = testPulsarMessage

        messageHandling.parseMessage(message)

        assertThat(receivedMessages).hasSize(1)
    }

    companion object {
        private const val MAX_MESSAGES_STORED = 500
    }
}
