package net.azisaba.azisababot.client.data

import java.io.InputStream
import java.io.OutputStream

fun <T> OutputStream.write(dataType: DataType<T>, value: T) {
    dataType.encode(this, value)
}

fun <T> InputStream.read(dataType: DataType<T>): T = dataType.decode(this)