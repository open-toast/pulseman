package com.toasttab.pulseman.entities

/**
 * Defines type of jar loader to use
 */
enum class JarLoaderType {
    BASE, // Only loads the jars in the jar loader
    GOOGLE_STANDARD, // Loads the google standard jars on top of the base jars
    PROTOKT // Loads the protokt jars on top of the base jars
}
