package com.toasttab.pulseman.entities

enum class SerializationFormat(val format: String) {
    TEXT("Text"),
    PROTOBUF("Protobuf");

    companion object {
        private val formatMapping = values().associateBy { it.format }

        fun fromFormat(format: String): SerializationFormat = formatMapping[format]!!
    }
}
