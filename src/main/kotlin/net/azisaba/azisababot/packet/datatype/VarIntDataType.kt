package net.azisaba.azisababot.packet.datatype

import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream

internal object VarIntDataType : DataType<Int> {
    override fun write(output: OutputStream, value: Int) {
        var value1 = value
        while (true) {
            if ((value1 and 0xFFFFFF80.toInt()) == 0) {
                output.write(value1)
                return
            }
            output.write(value1 and 0x7F or 0x80)
            value1 = value1 ushr 7
        }
    }

    override fun read(input: InputStream): Int {
        val dataInput = DataInputStream(input)

        var bytesRead = 0
        var result = 0
        var currentByte: Byte

        do {
            currentByte = dataInput.readByte()
            val value = (currentByte.toInt() and 0x7F)
            result = result or (value shl (7 * bytesRead))

            bytesRead++
            if (bytesRead > 5) {
                throw RuntimeException("VarInt too big")
            }
        } while ((currentByte.toInt() and 0x80) != 0)

        return result
    }
}