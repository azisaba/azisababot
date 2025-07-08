package net.azisaba.azisababot.packet.impl

import net.azisaba.azisababot.packet.Packet
import net.azisaba.azisababot.packet.ServerboundPingRequestPacket

internal class ServerboundPingRequestPacketImpl : PacketImpl(), ServerboundPingRequestPacket {
    override val packetId: Int = 0x01

    override val bound: Packet.Bound = Packet.Bound.SERVER

    override var timestamp: Long?
        get() = get(timestampField)
        set(value) = set(timestampField, value)

    private val timestampField: Packet.Field<Long> = long(0, "Timestamp", System.currentTimeMillis())
}