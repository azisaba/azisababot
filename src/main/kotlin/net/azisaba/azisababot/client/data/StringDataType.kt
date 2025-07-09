package net.azisaba.azisababot.client.data

import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream

internal object StringDataType : DataType<String> {
    override fun encode(output: OutputStream, value: String) {
        val bytes = value.toByteArray(Charsets.UTF_8)
        output.write(DataType.VAR_INT, bytes.size)
        output.write(bytes)
    }

    override fun decode(input: InputStream): String {
        val dataInput = DataInputStream(input)

        val length = input.read(DataType.VAR_INT)
        val bytes = ByteArray(length).also {
            dataInput.readFully(it)
        }
        return bytes.toString(Charsets.UTF_8)
    }
}