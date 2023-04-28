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
import com.toasttab.pulseman.pulsar.handlers.text.TextHandler
import com.toasttab.pulseman.state.PulsarSettings
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * Tests text serialization and deserialization - fake change
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PulsarTextMessageITest : PulsarITestSupport() {
    private lateinit var testTopic: String
    private lateinit var pulsarSettings: PulsarSettings

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
    }

    @ParameterizedTest(name = "characterSet:{0} input:{2} expectedOutput:{1}")
    @MethodSource("characterSetTestProvider")
    fun `Successfully serialize different text formats on an unauthenticated client`(
        characterSet: CharacterSet,
        expectedOutput: ByteArray,
        input: String
    ) {
        val response = responseFuture(testTopic)
        val textHandler = TextHandler(characterSet = characterSet)

        Pulsar(pulsarSettings) {}.sendMessage(textHandler.serialize(input))

        assertThat(response.get().data).isEqualTo(expectedOutput)
    }

    @ParameterizedTest(name = "characterSet:{0} input:{1} expectedOutput:{2}")
    @MethodSource("characterSetTestProvider")
    fun `Successfully deserialize different text formats on an unauthenticated client`(
        characterSet: CharacterSet,
        input: ByteArray,
        expectedOutput: String
    ) {
        val response = responseFuture(testTopic)
        val textHandler = TextHandler(characterSet = characterSet)

        Pulsar(pulsarSettings) {}.sendMessage(input)

        val message = response.get()
        val messageReceived = textHandler.deserialize(message.data)

        assertThat(messageReceived).isEqualTo(expectedOutput)
    }

    fun characterSetTestProvider(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(CharacterSet.UTF_8, utf8Bytes, "Test input UTF_8, $ €"),
            Arguments.of(CharacterSet.ISO_8859_1, iso88591Bytes, "Test input ISO_8859_1, $"),
            Arguments.of(CharacterSet.US_ASCII, usASCIIBytes, "Test input US_ASCII, $"),
            Arguments.of(CharacterSet.UTF_16, utf16Bytes, "Test input UTF_16, $ €"),
            Arguments.of(CharacterSet.UTF_16BE, utf16BEBytes, "Test input UTF_16BE, $ €"),
            Arguments.of(CharacterSet.UTF_16LE, utf16LEBytes, "Test input UTF_16LE, $ €"),
            Arguments.of(CharacterSet.BASE64, base64Bytes, "Test input Base64, $"),
        )
    }

    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        private val utf8Bytes = ubyteArrayOf(
            0x54U, 0x65U, 0x73U, 0x74U, 0x20U, 0x69U, 0x6eU, 0x70U, 0x75U, 0x74U, 0x20U, 0x55U, 0x54U, 0x46U, 0x5fU,
            0x38U, 0x2cU, 0x20U, 0x24U, 0x20U, 0xe2U, 0x82U, 0xacU
        ).asByteArray()

        @OptIn(ExperimentalUnsignedTypes::class)
        private val base64Bytes = ubyteArrayOf(
            0x54U, 0x65U, 0x73U, 0x74U, 0x20U, 0x69U, 0x6eU, 0x70U, 0x75U, 0x74U, 0x20U, 0x49U, 0x53U, 0x4fU, 0x5fU,
            0x38U, 0x38U, 0x35U, 0x39U, 0x5fU, 0x31U, 0x2cU, 0x20U, 0x24U
        ).asByteArray()

        @OptIn(ExperimentalUnsignedTypes::class)
        private val iso88591Bytes = ubyteArrayOf(
            0x54U, 0x65U, 0x73U, 0x74U, 0x20U, 0x69U, 0x6eU, 0x70U, 0x75U, 0x74U, 0x20U, 0x49U, 0x53U, 0x4fU, 0x5fU,
            0x38U, 0x38U, 0x35U, 0x39U, 0x5fU, 0x31U, 0x2cU, 0x20U, 0x24U
        ).asByteArray()

        @OptIn(ExperimentalUnsignedTypes::class)
        private val usASCIIBytes = ubyteArrayOf(
            0x54U, 0x65U, 0x73U, 0x74U, 0x20U, 0x69U, 0x6eU, 0x70U, 0x75U, 0x74U, 0x20U, 0x55U, 0x53U, 0x5fU, 0x41U,
            0x53U, 0x43U, 0x49U, 0x49U, 0x2cU, 0x20U, 0x24U
        ).asByteArray()

        @OptIn(ExperimentalUnsignedTypes::class)
        private val utf16Bytes = ubyteArrayOf(
            0xfeU, 0xffU, 0x00U, 0x54U, 0x00U, 0x65U, 0x00U, 0x73U, 0x00U, 0x74U, 0x00U, 0x20U, 0x00U, 0x69U, 0x00U,
            0x6eU, 0x00U, 0x70U, 0x00U, 0x75U, 0x00U, 0x74U, 0x00U, 0x20U, 0x00U, 0x55U, 0x00U, 0x54U, 0x00U, 0x46U,
            0x00U, 0x5fU, 0x00U, 0x31U, 0x00U, 0x36U, 0x00U, 0x2cU, 0x00U, 0x20U, 0x00U, 0x24U, 0x00U, 0x20U, 0x20U,
            0xacU
        ).asByteArray()

        @OptIn(ExperimentalUnsignedTypes::class)
        private val utf16BEBytes = ubyteArrayOf(
            0x00U, 0x54U, 0x00U, 0x65U, 0x00U, 0x73U, 0x00U, 0x74U, 0x00U, 0x20U, 0x00U, 0x69U, 0x00U, 0x6eU, 0x00U,
            0x70U, 0x00U, 0x75U, 0x00U, 0x74U, 0x00U, 0x20U, 0x00U, 0x55U, 0x00U, 0x54U, 0x00U, 0x46U, 0x00U, 0x5fU,
            0x00U, 0x31U, 0x00U, 0x36U, 0x00U, 0x42U, 0x00U, 0x45U, 0x00U, 0x2cU, 0x00U, 0x20U, 0x00U, 0x24U, 0x00U,
            0x20U, 0x20U, 0xacU
        ).asByteArray()

        @OptIn(ExperimentalUnsignedTypes::class)
        private val utf16LEBytes = ubyteArrayOf(
            0x54U, 0x00U, 0x65U, 0x00U, 0x73U, 0x00U, 0x74U, 0x00U, 0x20U, 0x00U, 0x69U, 0x00U, 0x6eU, 0x00U, 0x70U,
            0x00U, 0x75U, 0x00U, 0x74U, 0x00U, 0x20U, 0x00U, 0x55U, 0x00U, 0x54U, 0x00U, 0x46U, 0x00U, 0x5fU, 0x00U,
            0x31U, 0x00U, 0x36U, 0x00U, 0x4cU, 0x00U, 0x45U, 0x00U, 0x2cU, 0x00U, 0x20U, 0x00U, 0x24U, 0x00U, 0x20U,
            0x00U, 0xacU, 0x20U
        ).asByteArray()

        private val testPropertyString =
            """{
             "key1": "value1",
             "key2": "value2"
            }
            """.trimIndent()
    }
}
