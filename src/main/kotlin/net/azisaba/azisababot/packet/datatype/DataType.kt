package net.azisaba.azisababot.packet.datatype

import java.io.InputStream
import java.io.OutputStream

interface DataType<T> {
    fun write(output: OutputStream, value: T)

    fun read(input: InputStream): T

    companion object {
        val SHORT: DataType<Short> = ShortDataType
        val STRING: DataType<String> = StringDataType
        val VAR_INT: DataType<Int> = VarIntDataType
    }
}