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

import androidx.compose.runtime.mutableStateOf
import com.toasttab.pulseman.MultipleTypes
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.state.PulsarSettings
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Test all the functionality of the pulsar class
 *
 * TODO tests for authenticated connections
 * TODO failure paths
 * TODO split these pulsar tests into integration tests
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PulsarITest : PulsarITestSupport() {
    private lateinit var testTopic: String
    private lateinit var pulsarSettings: PulsarSettings
    private lateinit var runTimeJarLoader: RunTimeJarLoader

    private val testPropertyString =
        """{
             "key1": "value1",
             "key2": "value2"
           }
        """.trimIndent()
    private val testProperties = mapOf("key1" to "value1", "key2" to "value2")

    @BeforeAll
    fun init() {
        setUp()
        testTopic = initialTopicList.first()
        pulsarSettings = mockk(relaxed = true) {
            every { authSelector } returns mockk(relaxed = true) {
                every { selectedAuthClass } returns SingleSelection()
            }
            every { serviceUrl } returns mutableStateOf(pulsarContainer.pulsarBrokerUrl)
            every { topic } returns mutableStateOf(testTopic)
            every { propertySettings } returns mockk(relaxed = true) {
                every { propertyMap() } returns testPropertyString
            }
        }
        runTimeJarLoader = RunTimeJarLoader()
    }

    @Test
    fun `Successfully send a message on an unauthenticated client`() {
        val response = responseFuture(testTopic)
        val messageToSend = MultipleTypes()

        Pulsar(pulsarSettings, runTimeJarLoader) {}.sendMessage(messageToSend.toBytes())

        val message = response.get()
        val messageReceived = MultipleTypes.fromBytes(message.data)

        assertThat(message.properties).isEqualTo(testProperties)
        assertThat(messageReceived).isEqualTo(messageToSend)
    }

    @Test
    fun `Successfully receive a message on an unauthenticated client`() {
        lateinit var receivedBytes: ByteArray
        lateinit var receivedProperties: Map<String, String>

        val countDownLatch = CountDownLatch(1)
        val subscribeFuture = Pulsar(pulsarSettings, runTimeJarLoader) {}.createNewConsumer {
            receivedBytes = it.data
            receivedProperties = it.properties
            countDownLatch.countDown()
        }

        subscribeFuture?.get(10, TimeUnit.SECONDS)

        val messageToSend = MultipleTypes()
        sendMessage(messageToSend.toBytes(), testTopic, testProperties)

        countDownLatch.await()

        val messageReceived = MultipleTypes.fromBytes(receivedBytes)
        assertThat(receivedProperties).isEqualTo(testProperties)
        assertThat(messageReceived).isEqualTo(messageToSend)
    }
}
