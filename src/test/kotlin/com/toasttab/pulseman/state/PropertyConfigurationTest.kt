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

package com.toasttab.pulseman.state

import com.toasttab.pulseman.entities.TabValuesV3
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PropertyConfigurationTest {

    private fun createTabValuesV3(propertyMap: String? = null) = TabValuesV3(
        tabName = null,
        topic = null,
        serviceUrl = null,
        selectedAuthClass = null,
        authJsonParameters = null,
        propertyMap = propertyMap,
        serializationFormat = null,
        protobufSettings = null,
        textSettings = null,
        pulsarAdminURL = null,
        tabExtension = null,
        gradleScript = null,
        propertyFilters = emptyList(),
    )

    @Test
    fun `propertyMap returns empty map when text is blank`() {
        val userFeedback = mutableListOf<String>()
        val propertyConfiguration = PropertyConfiguration({}, null, { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).isEmpty()
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap returns empty map when text contains only whitespace`() {
        val userFeedback = mutableListOf<String>()
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3("   \n\t  "), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).isEmpty()
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap parses valid JSON into map`() {
        val userFeedback = mutableListOf<String>()
        val json = """{"environment": "production", "version": "1.0"}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).containsEntry("environment", "production")
        assertThat(result).containsEntry("version", "1.0")
        assertThat(result).hasSize(2)
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap parses empty JSON object into empty map`() {
        val userFeedback = mutableListOf<String>()
        val json = "{}"
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).isEmpty()
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap returns empty map on invalid JSON`() {
        val userFeedback = mutableListOf<String>()
        val invalidJson = """{"environment": "production", invalid}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(invalidJson), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).isEmpty()
        // Errors are reported on focus-lost, not during parsing
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap handles JSON with special characters`() {
        val userFeedback = mutableListOf<String>()
        val json = """{"key": "value with spaces", "key2": "value\"with\"quotes"}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).containsEntry("key", "value with spaces")
        assertThat(result).containsEntry("key2", "value\"with\"quotes")
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap handles JSON with numeric string values`() {
        val userFeedback = mutableListOf<String>()
        val json = """{"port": "8080", "version": "1.0.0"}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).containsEntry("port", "8080")
        assertThat(result).containsEntry("version", "1.0.0")
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap handles multiline JSON`() {
        val userFeedback = mutableListOf<String>()
        val json = """{
            "environment": "production",
            "version": "1.0",
            "region": "us-east-1"
        }"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).containsEntry("environment", "production")
        assertThat(result).containsEntry("version", "1.0")
        assertThat(result).containsEntry("region", "us-east-1")
        assertThat(result).hasSize(3)
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyText returns the text area content`() {
        val json = """{"key": "value"}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), {})

        val result = propertyConfiguration.propertyText()

        assertThat(result).isEqualTo(json)
    }

    @Test
    fun `propertyMap returns empty map when JSON contains arrays`() {
        val userFeedback = mutableListOf<String>()
        val json = """{"key": ["value1", "value2"]}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        // Should fail to parse because values must be strings, not arrays
        assertThat(result).isEmpty()
        // Errors are reported on focus-lost, not during parsing
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `propertyMap handles JSON with empty string values`() {
        val userFeedback = mutableListOf<String>()
        val json = """{"key1": "", "key2": "value"}"""
        val propertyConfiguration = PropertyConfiguration({}, createTabValuesV3(json), { userFeedback.add(it) })

        val result = propertyConfiguration.propertyMap()

        assertThat(result).containsEntry("key1", "")
        assertThat(result).containsEntry("key2", "value")
        assertThat(userFeedback).isEmpty()
    }

    @Test
    fun `initial settings default to empty JSON object when null`() {
        val userFeedback = mutableListOf<String>()
        val propertyConfiguration = PropertyConfiguration({}, null, { userFeedback.add(it) })

        val text = propertyConfiguration.propertyText()
        val result = propertyConfiguration.propertyMap()

        assertThat(text).isEqualTo("{\n}")
        assertThat(result).isEmpty()
        assertThat(userFeedback).isEmpty()
    }
}
