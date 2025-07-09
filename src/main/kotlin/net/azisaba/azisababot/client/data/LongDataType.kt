package net.azisaba.azisababot.client.data

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

internal object LongDataType : DataType<Long> {
    override fun encode(output: OutputStream, value: Long) {
        DataOutputStream(output).use { it.writeLong(value) }
    }

    override fun decode(input: InputStream): Long = DataInputStream(input).use { it.readLong() }
}