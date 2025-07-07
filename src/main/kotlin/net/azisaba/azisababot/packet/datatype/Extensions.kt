package net.azisaba.azisababot.packet.datatype

import java.io.InputStream
import java.io.OutputStream

fun <T> OutputStream.write(dataType: DataType<T>, value: T) {
    dataType.write(this, value)
}

fun <T> InputStream.read(dataType: DataType<T>): T = dataType.read(this)