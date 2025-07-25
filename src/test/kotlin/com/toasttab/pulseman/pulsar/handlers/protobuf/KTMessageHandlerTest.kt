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

package com.toasttab.pulseman.pulsar.handlers.protobuf

import com.toasttab.protokt.rt.KtMessage
import com.toasttab.protokt.rt.KtMessageSerializer
import com.toasttab.pulseman.entities.JarLoaderType
import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KTMessageHandlerTest {

    private lateinit var runTimeJarLoader: RunTimeJarLoader
    private lateinit var jarLoader: JarLoader

    @BeforeEach
    fun setup() {
        // Use a real JarLoader that uses the current classloader
        jarLoader = JarLoader(emptyArray())

        // Create a minimal mock for RunTimeJarLoader that returns our real jar loader
        runTimeJarLoader = mockk()
        every { runTimeJarLoader.getJarLoader(JarLoaderType.PROTOKT) } returns jarLoader
    }

    @Test
    fun `generateClassTemplate handles nested classes with dollar signs correctly`() {
        // Use the actual test nested message class
        val handler = KTMessageHandler(TestOuterMessage.TestInnerMessage::class.java, runTimeJarLoader)
        val template = handler.generateClassTemplate()

        // Verify the complete template output
        val expectedTemplate = """
            |import com.toasttab.pulseman.pulsar.handlers.protobuf.TestOuterMessage.TestInnerMessage
            |import java.lang.String
            |
            |TestInnerMessage {
            |${"\t"}innerField = //TODO
            |${"\t"}messageSize = //TODO
            |}
        """.trimMargin()
        assertThat(template).isEqualTo(expectedTemplate)
    }

    @Test
    fun `generateClassTemplate filters out ignored fields correctly`() {
        val handler = KTMessageHandler(TestMessageWithIgnoredFields::class.java, runTimeJarLoader)
        val template = handler.generateClassTemplate()

        // Verify only normal fields are included
        assertThat(template).contains("name = //TODO")
        assertThat(template).contains("value = //TODO")

        // Verify ignored fields are excluded
        assertThat(template).doesNotContain("\$\$delegatedProperties")
        assertThat(template).doesNotContain("messageSize\$delegate")
        assertThat(template).doesNotContain("unknownFields")
        assertThat(template).doesNotContain("Deserializer")
        assertThat(template).doesNotContain("\$stable")
    }

    @Test
    fun `generateClassTemplate handles field types with inner classes correctly`() {
        val handler = KTMessageHandler(TestMessageWithNestedField::class.java, runTimeJarLoader)
        val template = handler.generateClassTemplate()

        // Verify the field type import uses dots instead of dollar signs
        assertThat(template).contains("import com.toasttab.pulseman.pulsar.handlers.protobuf.TestOuterMessage.TestInnerMessage")
        assertThat(template).contains("nested = //TODO")
    }

    @Test
    fun `generateClassTemplate handles primitive types without adding imports`() {
        val handler = KTMessageHandler(TestMessageWithPrimitives::class.java, runTimeJarLoader)
        val template = handler.generateClassTemplate()

        // Verify primitive types are not imported
        assertThat(template).doesNotContain("import int")
        assertThat(template).doesNotContain("import boolean")
        assertThat(template).doesNotContain("import double")

        // Verify fields are present
        assertThat(template).contains("intField = //TODO")
        assertThat(template).contains("booleanField = //TODO")
        assertThat(template).contains("doubleField = //TODO")

        // Verify non-primitive fields have imports
        assertThat(template).contains("import java.lang.String")
        assertThat(template).contains("stringField = //TODO")
    }

    @Test
    fun `serialize delegates to KtMessage serialize method`() {
        val testMessage = TestSimpleMessage("test")
        val handler = KTMessageHandler(TestSimpleMessage::class.java, runTimeJarLoader)

        val result = handler.serialize(testMessage)

        assertThat(result).isEqualTo(byteArrayOf(1, 2, 3)) // As defined in TestSimpleMessage
    }

    @Test
    fun `prettyPrint formats message as JSON`() {
        val testMessage = TestSimpleMessage("hello world")
        val handler = KTMessageHandler(TestSimpleMessage::class.java, runTimeJarLoader)

        val result = handler.prettyPrint(testMessage)

        assertThat(result).contains("\"data\" : \"hello world\"")
    }
}

// Test classes that demonstrate the inner class handling
class TestOuterMessage {
    data class TestInnerMessage(
        val innerField: String,
        override val messageSize: Int = 0
    ) : KtMessage {
        override fun serialize(serializer: KtMessageSerializer) {}
    }
}

// Test message with fields that should be ignored
data class TestMessageWithIgnoredFields(
    val name: String,
    val value: Int,
    override val messageSize: Int = 0,
    @Suppress("PropertyName")
    val `$$delegatedProperties`: Any? = null,
    @Suppress("PropertyName")
    val `messageSize$delegate`: Any? = null,
    val unknownFields: Any? = null,
    @Suppress("unused")
    val deserializer: Any? = null
) : KtMessage {
    override fun serialize(serializer: KtMessageSerializer) {}
}

// Test message with a nested type field
data class TestMessageWithNestedField(
    val nested: TestOuterMessage.TestInnerMessage?,
    override val messageSize: Int = 0
) : KtMessage {
    override fun serialize(serializer: KtMessageSerializer) {}
}

// Test message with primitive fields
data class TestMessageWithPrimitives(
    val intField: Int,
    val booleanField: Boolean,
    val doubleField: Double,
    val stringField: String,
    override val messageSize: Int = 0
) : KtMessage {
    override fun serialize(serializer: KtMessageSerializer) {}
}

// Simple test message for serialization
data class TestSimpleMessage(
    val data: String,
    override val messageSize: Int = 0
) : KtMessage {
    override fun serialize(serializer: KtMessageSerializer) {}

    override fun serialize(): ByteArray = byteArrayOf(1, 2, 3)
}
