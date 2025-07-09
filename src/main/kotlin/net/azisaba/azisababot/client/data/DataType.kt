package net.azisaba.azisababot.client.data

import java.io.InputStream
import java.io.OutputStream

interface DataType<T> {
    fun encode(output: OutputStream, value: T)

    fun decode(input: InputStream): T

    companion object {
        val LONG: DataType<Long> = LongDataType
        val SHORT: DataType<Short> = ShortDataType
        val STRING: DataType<String> = StringDataType
        val UNSIGNED_SHORT: DataType<UShort> = UShortDataType
        val VAR_INT: DataType<Int> = VarIntDataType
    }
}