package net.azisaba.azisababot.packet.impl

import net.azisaba.azisababot.packet.ClientboundPongResponsePacket
import net.azisaba.azisababot.packet.Packet

internal class ClientboundPongResponsePacketImpl : PacketImpl(), ClientboundPongResponsePacket {
    override val packetId: Int = 0x01

    override val bound: Packet.Bound = Packet.Bound.CLIENT

    override var timestamp: Long?
        get() = get(timestampField)
        set(value) = set(timestampField, value)

    private val timestampField: Packet.Field<Long> = long(0, "Timestamp")
}