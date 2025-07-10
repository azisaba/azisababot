package net.azisaba.azisababot.client.packet

import net.azisaba.azisababot.client.data.DataType
import net.azisaba.azisababot.client.data.read
import net.azisaba.azisababot.client.data.write
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream

@Suppress("UNCHECKED_CAST")
class PacketImpl(override val packetType: Packet.Type) : Packet {
    private val fields: MutableMap<Packet.Field<*>, Any> = mutableMapOf()

    override fun <T> get(key: Packet.Field<T>): T? = fields[key] as? T

    override fun <T> set(key: Packet.Field<T>, value: T?) {
        if (value != null) {
            fields[key] = value
        } else {
            fields.remove(key)
        }
    }

    override fun send(output: OutputStream) {
        val packet = ByteArrayOutputStream()
        writeTo(packet)
        output.write(packet.toByteArray())
        output.flush()
    }

    override fun writeTo(output: OutputStream) {
        val body = ByteArrayOutputStream()
        with(body) {
            write(DataType.VAR_INT, packetType.packetId)
            for (field in packetType.fields) {
                val value = get(field) ?: throw IllegalStateException("Missing required field: $field")
                write(field.dataType as DataType<Any>, value)
            }
        }

        val bodyBytes = body.toByteArray()
        with(output) {
            write(DataType.VAR_INT, bodyBytes.size)
            write(bodyBytes)
            flush()
        }
    }

    override fun readFrom(input: InputStream) {
        val dataInput = DataInputStream(input)

        val length = input.read(DataType.VAR_INT)
        val body = ByteArray(length)
        dataInput.readFully(body)

        val bodyInput = body.inputStream()

        val packetId = bodyInput.read(DataType.VAR_INT)
        require(packetId == packetType.packetId) { "Invalid packet ID: excepted ${packetType.packetId} but got $packetId" }

        for (field in packetType.fields) {
            set(field as Packet.Field<Any>, field.dataType.decode(bodyInput))
        }
    }

    abstract class TypeImpl : Packet.Type {
        override val fields: List<Packet.Field<*>>
            get() = _fields.toList()

        private val _fields: MutableList<Packet.Field<*>> = mutableListOf()

        protected fun <T> field(name: String, dataType: DataType<T>, defaultValue: T? = null): Packet.Field<T> =
            FieldImpl(name, dataType, defaultValue).also {
                _fields += it
            }

        protected fun long(name: String, defaultValue: Long? = null): Packet.Field<Long> = field(name, DataType.LONG, defaultValue)

        protected fun short(name: String, defaultValue: Short? = null): Packet.Field<Short> = field(name, DataType.SHORT, defaultValue)

        protected fun string(name: String, defaultValue: String? = null): Packet.Field<String> = field(name, DataType.STRING, defaultValue)

        protected fun ushort(name: String, defaultValue: UShort? = null): Packet.Field<UShort> = field(name, DataType.UNSIGNED_SHORT, defaultValue)

        protected fun varInt(name: String, defaultValue: Int? = null): Packet.Field<Int> = field(name, DataType.VAR_INT, defaultValue)
    }

    private data class FieldImpl<T>(
        override val name: String,
        override val dataType: DataType<T>,
        override val defaultValue: T? = null
    ) : Packet.Field<T>
}