package com.toasttab.pulseman.entities

import com.toasttab.pulseman.jars.JarLoader
import com.toasttab.pulseman.pulsar.handlers.PulsarMessageClassInfo

class CompileResult(
    val code: String,
    val classToGenerate: PulsarMessageClassInfo,
    val jarLoader: JarLoader,
    val bytes: ByteArray
)
