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

import com.toasttab.pulseman.AppStrings.EXCEPTION
import com.toasttab.pulseman.AppStrings.GENERATED_CLASS_NOT_SAME_AS_SELECTED
import com.toasttab.pulseman.AppStrings.NO_CLASS_SELECTED
import com.toasttab.pulseman.AppStrings.SUCCESSFULLY_COMPILED_CLASS
import com.toasttab.pulseman.entities.SingleSelection
import com.toasttab.pulseman.jars.RunTimeJarLoader
import com.toasttab.pulseman.jars.RunTimeJarLoader.addJarsToClassLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessage
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * This handles the generation on messaging classes at run time.
 * It uses the kotlin scripting api and the JSR 223 spec.
 * Some basic examples of how this works can be found here.
 * https://github.com/Kotlin/kotlin-script-examples
 *
 * The final return of any code must contain the class to be serialized.
 */
object KotlinScripting {
    fun compileMessage(
        code: String,
        selectedClass: SingleSelection<PulsarMessage>,
        setUserFeedback: (String) -> Unit
    ): ByteArray? {
        val classToGenerate = selectedClass.selected
        if (classToGenerate == null) {
            setUserFeedback(NO_CLASS_SELECTED)
            return null
        }

        try {
            addJarsToClassLoader()
            val engine: ScriptEngine = ScriptEngineManager(RunTimeJarLoader.loader).getEngineByExtension(KTS_EXTENSION)

            val generatedClass = engine.eval(code)
            if (generatedClass.javaClass.name != classToGenerate.cls.name) {
                setUserFeedback(GENERATED_CLASS_NOT_SAME_AS_SELECTED)
                return null
            }
            setUserFeedback(SUCCESSFULLY_COMPILED_CLASS)
            return classToGenerate.serialize(generatedClass)
        } catch (ex: Throwable) {
            setUserFeedback("$EXCEPTION:\n$ex")
        }
        return null
    }

    private const val KTS_EXTENSION = "kts"
}
