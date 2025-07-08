package net.azisaba.azisababot.packet

import net.azisaba.azisababot.packet.datatype.DataType
import net.azisaba.azisababot.packet.impl.*
import net.azisaba.azisababot.packet.impl.ClientboundPongResponsePacketImpl
import net.azisaba.azisababot.packet.impl.ClientboundStatusResponsePacketImpl
import net.azisaba.azisababot.packet.impl.ServerboundHandshakePacketImpl
import net.azisaba.azisababot.packet.impl.ServerboundStatusRequestPacketImpl
import java.io.InputStream
import java.io.OutputStream

interface Packet : Iterable<Packet.Field<*>> {
    val packetId: Int

    val bound: Bound

    operator fun <T> get(key: Field<T>): T?

    operator fun <T> set(key: Field<T>, value: T?)

    fun send(output: OutputStream)

    fun writeTo(output: OutputStream)

    fun readFrom(input: InputStream)

    companion object {
        fun clientboundPongResponse(): ClientboundPongResponsePacket = ClientboundPongResponsePacketImpl()

        fun clientboundStatusResponse(): ClientboundStatusResponsePacket = ClientboundStatusResponsePacketImpl()

        fun serverboundHandshake(): ServerboundHandshakePacket = ServerboundHandshakePacketImpl()

        fun serverboundPingRequest(): ServerboundPingRequestPacket = ServerboundPingRequestPacketImpl()

        fun serverboundStatusRequest(): ServerboundStatusRequestPacket = ServerboundStatusRequestPacketImpl()
    }

    interface Field<T> {
        val index: Int

        val name: String

        val dataType: DataType<T>
    }

    enum class Bound {
        CLIENT,
        SERVER
    }
}