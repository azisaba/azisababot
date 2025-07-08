package net.azisaba.azisababot.packet.datatype

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

internal object LongDataType : DataType<Long> {
    override fun write(output: OutputStream, value: Long) {
        DataOutputStream(output).use { it.writeLong(value) }
    }

    override fun read(input: InputStream): Long = DataInputStream(input).use { it.readLong() }
}