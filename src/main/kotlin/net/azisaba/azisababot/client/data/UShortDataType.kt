package net.azisaba.azisababot.client.data

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

internal object UShortDataType : DataType<UShort> {
    override fun encode(output: OutputStream, value: UShort) {
        val intValue = value.toInt()
        output.write((intValue shr 8) and 0xFF)
        output.write(intValue and 0xFF)
    }

    override fun decode(input: InputStream): UShort {
        val high = input.read()
        val low = input.read()
        if (high == -1 || low == -1) throw EOFException("Unexpected end of stream")
        return ((high shl 8) or low).toUShort()
    }
}