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
import com.toasttab.pulseman.entities.CharacterSet
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.text.TextHandler
import com.toasttab.pulseman.state.PulsarSettings
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Tests text serialization and deserialization
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PulsarTextMessageITest : PulsarITestSupport() {
    private lateinit var testTopic: String
    private lateinit var pulsarSettings: PulsarSettings
    private lateinit var runTimeJarLoader: RunTimeJarLoader

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

    @ParameterizedTest(name = "characterSet:{0} input:{2} expectedOutput:{1}")
    @MethodSource("com.toasttab.pulseman.TestByteArrays#characterSetTestProvider")
    fun `Successfully serialize different text formats on an unauthenticated client`(
        characterSet: CharacterSet,
        expectedOutput: ByteArray,
        input: String
    ) {
        val response = responseFuture(testTopic)
        val textHandler = TextHandler(characterSet = characterSet)

        Pulsar(pulsarSettings, runTimeJarLoader) {}.sendMessage(textHandler.serialize(input))

        assertThat(response.get().data).isEqualTo(expectedOutput)
    }

    @ParameterizedTest(name = "characterSet:{0} input:{1} expectedOutput:{2}")
    @MethodSource("com.toasttab.pulseman.TestByteArrays#characterSetTestProvider")
    fun `Successfully deserialize different text formats on an unauthenticated client`(
        characterSet: CharacterSet,
        input: ByteArray,
        expectedOutput: String
    ) {
        val response = responseFuture(testTopic)
        val textHandler = TextHandler(characterSet = characterSet)

        Pulsar(pulsarSettings, runTimeJarLoader) {}.sendMessage(input)

        val message = response.get()
        val messageReceived = textHandler.deserialize(message.data)

        assertThat(messageReceived).isEqualTo(expectedOutput)
    }

    companion object {
        private val testPropertyString =
            """{
             "key1": "value1",
             "key2": "value2"
            }
            """.trimIndent()
    }
}
