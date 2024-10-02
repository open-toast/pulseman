package state.protocol.protobuf

import com.toasttab.pulseman.MultipleTypes
import com.toasttab.pulseman.state.protocol.protobuf.ConvertType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Base64

class ConvertProtobufMessageTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `Convert hex to a class`() {
        val testType = MultipleTypes()
        val hexInput = testType.toBytes().toHexString()
        val bytes = ConvertType.HEX.toBytes(hexInput) {}
        assertThat(MultipleTypes.fromBytes(bytes)).isEqualTo(testType)
    }

    @Test
    fun `Convert base64 to a class`() {
        val testType = MultipleTypes()
        val base64Input = Base64.getEncoder().encodeToString(testType.toBytes())
        val bytes = ConvertType.BASE64.toBytes(base64Input) {}
        assertThat(MultipleTypes.fromBytes(bytes)).isEqualTo(testType)
    }
}
