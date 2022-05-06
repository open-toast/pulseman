package com.toasttab.pulseman.entities

// Taken from import java.nio.charset.Charset
enum class CharacterSet(val charSet: String) {
    US_ASCII("US-ASCII"),
    ISO_8859_1("ISO-8859-1"),
    UTF_8("UTF-8"),
    UTF_16BE("UTF-16BE"),
    UTF_16LE("UTF-16LE"),
    UTF_16("UTF-16");

    companion object {
        private val charSetMapping = values().associateBy { it.charSet }

        fun fromCharSet(charSet: String): CharacterSet = charSetMapping[charSet]!!
    }
}
