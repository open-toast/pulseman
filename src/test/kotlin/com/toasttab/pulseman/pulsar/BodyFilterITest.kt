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
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.toasttab.pulseman.MultipleTypes
import com.toasttab.pulseman.MultipleTypesPulsarMessage
import com.toasttab.pulseman.entities.ActiveBodyFilter
import com.toasttab.pulseman.entities.ReceivedMessages
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import com.toasttab.pulseman.scripting.KotlinScripting
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
 * Integration tests for the body filter feature.
 * Uses a real Pulsar container (via PulsarITestSupport) to subscribe to a topic,
 * send messages, and verify that the compiled Kotlin predicate correctly passes or skips messages.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BodyFilterITest : PulsarITestSupport() {
    private lateinit var testTopic: String
    private lateinit var pulsarSettings: PulsarSettings
    private lateinit var runTimeJarLoader: RunTimeJarLoader

    @BeforeAll
    fun init() {
        setUp()
        // Use a topic distinct from PulsarITest (which uses initialTopicList.first())
        testTopic = initialTopicList[1]
        pulsarSettings = mockk(relaxed = true) {
            every { authSelector } returns mockk(relaxed = true) {
                every { selectedAuthClass } returns SingleSelection()
            }
            every { serviceUrl } returns mutableStateOf(pulsarContainer.pulsarBrokerUrl)
            every { topic } returns mutableStateOf(testTopic)
            every { propertySettings } returns mockk(relaxed = true) {
                every { propertyMap() } returns emptyMap()
            }
        }
        runTimeJarLoader = RunTimeJarLoader()
    }

    @Test
    fun `body filter passes matching messages and skips non-matching messages`() {
        val receivedMessages = SnapshotStateList<ReceivedMessages>()
        val feedback = mutableListOf<String>()

        val pulsarMessage = MultipleTypesPulsarMessage(MultipleTypes::class.java, runTimeJarLoader)
        val selectedClass = SingleSelection<PulsarMessageClassInfo>().apply {
            selected = pulsarMessage
        }

        // Compile a predicate that only passes messages where b == true
        val jarLoader = pulsarMessage.getJarLoader()
        val predicate = KotlinScripting.compilePredicate(
            code = """
                import com.toasttab.pulseman.MultipleTypes
                { body: Any -> (body as MultipleTypes).b == true }
            """.trimIndent(),
            jarLoader = jarLoader,
            setUserFeedback = { feedback.add(it) }
        )
        assertThat(predicate).isNotNull()
        val activeFilter = predicate?.let { ActiveBodyFilter(it) }

        val messageHandling = MessageHandlingClassImpl(
            selectedProtoClass = selectedClass,
            propertyFilter = { emptyMap() },
            receivedMessages = receivedMessages,
            setUserFeedback = { feedback.add(it) },
            bodyFilter = { activeFilter }
        )

        // Count down once per Pulsar message delivered (regardless of filter outcome)
        val allDelivered = CountDownLatch(2)

        val subscribeFuture = Pulsar(pulsarSettings, runTimeJarLoader) {}.createNewConsumer { message ->
            messageHandling.parseMessage(message)
            allDelivered.countDown()
        }
        subscribeFuture?.get(10, TimeUnit.SECONDS)

        // This message has b=false — should be skipped by the predicate
        sendMessage(MultipleTypes(b = false).toBytes(), testTopic, emptyMap())
        // This message has b=true — should pass through the predicate
        sendMessage(MultipleTypes(b = true).toBytes(), testTopic, emptyMap())

        assertThat(allDelivered.await(10, TimeUnit.SECONDS)).isTrue()
        assertThat(receivedMessages).hasSize(1)
        assertThat(messageHandling.skippedMessages).isEqualTo(1)
    }

    @Test
    fun `body filter with always-false predicate skips all messages`() {
        val receivedMessages = SnapshotStateList<ReceivedMessages>()
        val feedback = mutableListOf<String>()

        val pulsarMessage = MultipleTypesPulsarMessage(MultipleTypes::class.java, runTimeJarLoader)
        val selectedClass = SingleSelection<PulsarMessageClassInfo>().apply {
            selected = pulsarMessage
        }

        val jarLoader = pulsarMessage.getJarLoader()
        val predicate = KotlinScripting.compilePredicate(
            code = "{ body: Any -> false }",
            jarLoader = jarLoader,
            setUserFeedback = { feedback.add(it) }
        )
        assertThat(predicate).isNotNull()
        val activeFilter = predicate?.let { ActiveBodyFilter(it) }

        val messageHandling = MessageHandlingClassImpl(
            selectedProtoClass = selectedClass,
            propertyFilter = { emptyMap() },
            receivedMessages = receivedMessages,
            setUserFeedback = { feedback.add(it) },
            bodyFilter = { activeFilter }
        )

        val allDelivered = CountDownLatch(2)

        val subscribeFuture = Pulsar(pulsarSettings, runTimeJarLoader) {}.createNewConsumer { message ->
            messageHandling.parseMessage(message)
            allDelivered.countDown()
        }
        subscribeFuture?.get(10, TimeUnit.SECONDS)

        sendMessage(MultipleTypes().toBytes(), testTopic, emptyMap())
        sendMessage(MultipleTypes().toBytes(), testTopic, emptyMap())

        assertThat(allDelivered.await(10, TimeUnit.SECONDS)).isTrue()
        assertThat(receivedMessages).isEmpty()
        assertThat(messageHandling.skippedMessages).isEqualTo(2)
    }
}
