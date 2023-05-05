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

package com.toasttab.pulseman.pulsar.handlers.text

import com.toasttab.pulseman.entities.CharacterSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Tests text serialization and deserialization
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TextHandlerTest {

    @ParameterizedTest(name = "characterSet:{0} input:{2} expectedOutput:{1}")
    @MethodSource("com.toasttab.pulseman.TestByteArrays#characterSetTestProvider")
    fun `Successfully serialize different text formats on an unauthenticated client`(
        characterSet: CharacterSet,
        expectedOutput: ByteArray,
        input: String
    ) {
        val textHandler = TextHandler(characterSet = characterSet)
        val serializedBytes = textHandler.serialize(input)
        assertThat(serializedBytes).isEqualTo(expectedOutput)
    }

    @ParameterizedTest(name = "characterSet:{0} input:{1} expectedOutput:{2}")
    @MethodSource("com.toasttab.pulseman.TestByteArrays#characterSetTestProvider")
    fun `Successfully deserialize different text formats on an unauthenticated client`(
        characterSet: CharacterSet,
        input: ByteArray,
        expectedOutput: String
    ) {
        val textHandler = TextHandler(characterSet = characterSet)
        val deserializedString = textHandler.deserialize(input)
        assertThat(deserializedString).isEqualTo(expectedOutput)
    }
}
