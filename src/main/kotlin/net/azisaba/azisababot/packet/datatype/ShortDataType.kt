package net.azisaba.azisababot.packet.datatype

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

internal object ShortDataType : DataType<Short> {
    override fun write(output: OutputStream, value: Short) {
        output.write((value.toInt() shr 8) and 0xFF)
        output.write(value.toInt() and 0xFF)
    }

    override fun read(input: InputStream): Short {
        val high = input.read()
        val low = input.read()
        if (high == -1 || low == -1) throw EOFException("Unexpected end of stream")
        return ((high shl 8) or low).toShort()
    }
}