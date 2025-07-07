package net.azisaba.azisababot.packet.impl

import net.azisaba.azisababot.packet.ClientboundStatusResponsePacket
import net.azisaba.azisababot.packet.Packet

internal class ClientboundStatusResponsePacketImpl : PacketImpl(), ClientboundStatusResponsePacket {
    override val packetId: Int = 0x00

    override val bound: Packet.Bound = Packet.Bound.CLIENT

    override var jsonResponse: String?
        get() = get(jsonResponseField)
        set(value) = set(jsonResponseField, value)

    private val jsonResponseField: Packet.Field<String> = string(0, "JSON Response")
}