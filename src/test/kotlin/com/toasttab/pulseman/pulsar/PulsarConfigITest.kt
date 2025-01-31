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

import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.RunTimeJarLoader
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PulsarConfigITest : PulsarITestSupport() {
    @BeforeAll
    fun init() {
        setUp()
    }

    @Test
    fun `PulsarSettings retrieves a list of topics correctly`() {
        val topicList = PulsarConfig(runTimeJarLoader = RunTimeJarLoader()) {}.getTopics(
            pulsarUrl = pulsarContainer.httpServiceUrl,
            pulsarSettings = mockk {
                every { authSelector } returns mockk {
                    every { selectedAuthClass } returns SingleSelection()
                }
            }
        )
        assertThat(topicList).hasSameElementsAs(initialTopicList)
    }
}
