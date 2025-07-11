package net.azisaba.azisababot.client.packet

import net.azisaba.azisababot.client.data.DataType
import java.io.InputStream
import java.io.OutputStream

interface Packet : PacketReader, PacketWriter {
    val packetType: Type

    operator fun <T> get(key: Field<T>): T?

    operator fun <T> set(key: Field<T>, value: T?)

    fun send(output: OutputStream)

    companion object {
        fun packet(packetType: Type): Packet = PacketImpl(packetType)

        fun packet(packetType: Type, writer: Packet.() -> Unit): Packet = packet(packetType).apply(writer)

        fun packet(packetType: Type, input: InputStream) = packet(packetType).apply {
            readFrom(input)
        }
    }

    interface Type {
        val packetId: Int

        val fields: List<Field<*>>
    }

    interface Field<T> {
        val name: String

        val dataType: DataType<T>

        val defaultValue: T?
    }
}