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

import io.mockk.every
import io.mockk.mockk
import org.apache.pulsar.client.api.Message
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MessageHandlingTest {

    private val messageHandling = object : MessageHandling {
        override val skippedMessages: Int = 0
        override fun resetSkippedMessages() {}
        override fun parseMessage(message: Message<ByteArray>) {
            // No-op implementation for testing
        }
    }

    @Test
    fun `skipMessage returns false when propertyFilter is empty`() {
        val message = mockk<Message<ByteArray>>()
        val propertyFilter = emptyMap<String, String>()

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isFalse
    }

    @Test
    fun `skipMessage returns false when all filter properties match`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "environment" to "production",
            "version" to "1.0"
        )
        val propertyFilter = mapOf(
            "environment" to "production",
            "version" to "1.0"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isFalse
    }

    @Test
    fun `skipMessage returns true when filter property does not match`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf("environment" to "staging")
        val propertyFilter = mapOf("environment" to "production")

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isTrue
    }

    @Test
    fun `skipMessage returns false when one of multiple filters matches`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "environment" to "production"
        )
        val propertyFilter = mapOf(
            "environment" to "production",
            "version" to "1.0"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isFalse
    }

    @Test
    fun `skipMessage returns true when none of multiple filters match`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "environment" to "staging",
            "version" to "2.0"
        )
        val propertyFilter = mapOf(
            "environment" to "production",
            "version" to "1.0"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isTrue
    }

    @Test
    fun `skipMessage returns true when message properties are empty`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns emptyMap()
        val propertyFilter = mapOf(
            "environment" to "production"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isTrue
    }

    @Test
    fun `skipMessage returns false when message has extra properties not in filter`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "environment" to "production",
            "version" to "1.0",
            "region" to "us-east-1"
        )
        val propertyFilter = mapOf(
            "environment" to "production",
            "version" to "1.0"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isFalse
    }

    @Test
    fun `skipMessage returns false when at least one of multiple filter properties matches`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "environment" to "production",
            "version" to "1.0",
            "region" to "us-west-1"
        )
        val propertyFilter = mapOf(
            "environment" to "production",
            "version" to "1.0",
            "region" to "us-east-1"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isFalse
    }

    @Test
    fun `skipMessage is case-sensitive for property values`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "environment" to "Production"
        )
        val propertyFilter = mapOf(
            "environment" to "production"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isTrue
    }

    @Test
    fun `skipMessage is case-sensitive for property keys`() {
        val message = mockk<Message<ByteArray>>()
        every { message.properties } returns mapOf(
            "Environment" to "production"
        )
        val propertyFilter = mapOf(
            "environment" to "production"
        )

        val result = messageHandling.skipMessage(message, propertyFilter)

        assertThat(result).isTrue
    }
}
