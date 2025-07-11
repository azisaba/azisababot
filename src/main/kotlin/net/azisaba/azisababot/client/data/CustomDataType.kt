package net.azisaba.azisababot.client.data

import java.io.InputStream
import java.io.OutputStream

interface CustomDataType<T, P> : DataType<T> {
    val primitive: DataType<P>

    fun toPrimitive(value: T): P

    fun fromPrimitive(value: P): T

    override fun encode(output: OutputStream, value: T) {
        val primitiveValue = toPrimitive(value)
        primitive.encode(output, primitiveValue)
    }

    override fun decode(input: InputStream): T {
        val primitiveValue = primitive.decode(input)
        return fromPrimitive(primitiveValue)
    }
}