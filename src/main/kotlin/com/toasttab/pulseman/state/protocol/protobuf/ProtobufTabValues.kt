package com.toasttab.pulseman.state.protocol.protobuf

data class ProtobufTabValues(
    val code: String?,
    val selectedClassSend: String?,
    val selectedClassReceive: List<String>
)
