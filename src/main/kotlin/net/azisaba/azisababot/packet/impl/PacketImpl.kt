package net.azisaba.azisababot.packet.impl

import net.azisaba.azisababot.packet.Packet
import net.azisaba.azisababot.packet.datatype.DataType
import net.azisaba.azisababot.packet.datatype.read
import net.azisaba.azisababot.packet.datatype.write
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream

@Suppress("UNCHECKED_CAST")
internal abstract class PacketImpl : Packet {
    private val fields: MutableMap<Packet.Field<*>, Any?> = mutableMapOf()

    override fun <T> get(key: Packet.Field<T>): T? = fields[key] as? T

    override fun <T> set(key: Packet.Field<T>, value: T?) {
        fields[key] = value
    }

    override fun iterator(): Iterator<Packet.Field<*>> = fields.keys.sortedBy { it.index }.iterator()

    override fun send(output: OutputStream) {
        check(bound == Packet.Bound.SERVER) { "This packet cannot be sent" }
        val bytes = ByteArrayOutputStream().also {
            writeTo(it)
        }
        output.write(bytes.toByteArray())
        output.flush()
    }

    override fun writeTo(output: OutputStream) {
        val body = ByteArrayOutputStream()
        body.write(DataType.VAR_INT, packetId)
        for (field in this) {
            val value = get(field) ?: throw IllegalStateException("Missing required field: ${field.name}")
            body.write(field.dataType as DataType<Any>, value)
        }

        val packetBytes = body.toByteArray()

        output.write(DataType.VAR_INT, packetBytes.size)
        output.write(packetBytes)
        output.flush()
    }

    override fun readFrom(input: InputStream) {
        val dataInput = DataInputStream(input)

        val length = input.read(DataType.VAR_INT)
        val body = ByteArray(length)

        dataInput.readFully(body)

        val stream = body.inputStream()

        val packetId = stream.read(DataType.VAR_INT)
        require(packetId == this.packetId) { "Invalid packet ID: excepted ${this.packetId} but got $packetId" }

        for (field in this) {
            set(field as Packet.Field<Any>, field.dataType.read(stream))
        }
    }

    protected fun long(index: Int, name: String, default: Long? = null): Packet.Field<Long> =
        defineField(index, name, DataType.LONG, default)

    protected fun short(index: Int, name: String, default: Short? = null): Packet.Field<Short> =
        defineField(index, name, DataType.SHORT, default)

    protected fun string(index: Int, name: String, default: String? = null): Packet.Field<String> =
        defineField(index, name, DataType.STRING, default)

    protected fun varInt(index: Int, name: String, default: Int? = null): Packet.Field<Int> =
        defineField(index, name, DataType.VAR_INT, default)

    private fun <T> defineField(index: Int, name: String, dataType: DataType<T>, default: T?): Packet.Field<T> {
        val field = FieldImpl(index, name, dataType)
        fields[field] = default
        return field
    }

    private data class FieldImpl<T>(
        override val index: Int,
        override val name: String,
        override val dataType: DataType<T>
    ) : Packet.Field<T>
}