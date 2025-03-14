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

package com.toasttab.pulseman.scripting

import com.toasttab.pulseman.MultipleTypes
import com.toasttab.pulseman.MultipleTypesPulsarMessage
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinScriptingTest {

    @Test
    fun `Compiling a kotlin class works`() {
        val pulsarMessage = MultipleTypesPulsarMessage(MultipleTypes::class.java, RunTimeJarLoader())
        val selectedClass = SingleSelection<PulsarMessageClassInfo>().apply {
            selected = pulsarMessage
        }

        val compileResult = KotlinScripting.compileMessage(
            code = """
                import com.toasttab.pulseman.MultipleTypes
                
                MultipleTypes()
            """.trimIndent(),
            selectedClass = selectedClass,
            setUserFeedback = { }
        )

        val generatedClass = MultipleTypes.fromBytes(compileResult?.bytes!!)
        assertThat(generatedClass).isEqualTo(MultipleTypes())
    }

    @Test
    fun `Recompiling a kotlin class works`() {
        val pulsarMessage = MultipleTypesPulsarMessage(MultipleTypes::class.java, RunTimeJarLoader())
        val selectedClass = SingleSelection<PulsarMessageClassInfo>().apply {
            selected = pulsarMessage
        }

        val compileResult = KotlinScripting.compileMessage(
            code = """
                import com.toasttab.pulseman.MultipleTypes
                
                MultipleTypes()
            """.trimIndent(),
            selectedClass = selectedClass,
            setUserFeedback = { }
        )

        val generatedClass = MultipleTypes.fromBytes(compileResult?.bytes!!)
        assertThat(generatedClass).isEqualTo(MultipleTypes())

        val recompiledBytes = KotlinScripting.recompile(
            compileInfo = compileResult,
            setUserFeedback = { }
        )

        val recompiledClass = MultipleTypes.fromBytes(recompiledBytes?.bytes!!)
        assertThat(recompiledClass).isEqualTo(MultipleTypes())
    }
}
