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

    @Test
    fun `generateFilterTemplate generates predicate with default values for primitives`() {
        val handler = KTMessageHandler(TestMessageWithPrimitives::class.java, runTimeJarLoader)
        val template = handler.generateFilterTemplate()

        assertThat(template).contains("// Must return a Boolean")
        assertThat(template).contains("{ body: TestMessageWithPrimitives ->")
        assertThat(template).contains("body.intField == 0")
        assertThat(template).contains("body.booleanField == false")
        assertThat(template).contains("body.doubleField == 0.0")
        assertThat(template).contains("body.stringField == \"\"")
    }

    @Test
    fun `generateFilterTemplate uses safe calls for nested KtMessage fields`() {
        val handler = KTMessageHandler(TestMessageWithNestedField::class.java, runTimeJarLoader)
        val template = handler.generateFilterTemplate()

        assertThat(template).contains("body.nested?.innerField")
    }

    @Test
    fun `generateFilterTemplate filters out ignored fields`() {
        val handler = KTMessageHandler(TestMessageWithIgnoredFields::class.java, runTimeJarLoader)
        val template = handler.generateFilterTemplate()

        assertThat(template).contains("body.name == \"\"")
        assertThat(template).contains("body.value == 0")
        assertThat(template).doesNotContain("unknownFields")
        assertThat(template).doesNotContain("Deserializer")
    }

    @Test
    fun `generateFilterTemplate recurses through 3 levels of nested messages`() {
        val handler = KTMessageHandler(TestLevel1::class.java, runTimeJarLoader)
        val template = handler.generateFilterTemplate()

        // Level 1 -> Level 2
        assertThat(template).contains("body.level2?.name == \"\"")
        // Level 2 -> Level 3
        assertThat(template).contains("body.level2?.level3?.value == 0")
        // Level 3 leaf field
        assertThat(template).contains("body.level2?.level3?.active == false")
        // Level 1 own field
        assertThat(template).contains("body.id == \"\"")
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

// Test messages for 3-level nesting
data class TestLevel1(
    val id: String,
    val level2: TestLevel2?,
    override val messageSize: Int = 0
) : KtMessage {
    override fun serialize(serializer: KtMessageSerializer) {}
}

data class TestLevel2(
    val name: String,
    val level3: TestLevel3?,
    override val messageSize: Int = 0
) : KtMessage {
    override fun serialize(serializer: KtMessageSerializer) {}
}

data class TestLevel3(
    val value: Int,
    val active: Boolean,
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
