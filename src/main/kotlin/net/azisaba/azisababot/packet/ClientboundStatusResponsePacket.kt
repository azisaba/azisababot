package net.azisaba.azisababot.packet

import net.azisaba.azisababot.packet.impl.ClientboundStatusResponsePacketImpl

interface ClientboundStatusResponsePacket : Packet {
    var jsonResponse: String?

    companion object {
        fun create(): ClientboundStatusResponsePacket = ClientboundStatusResponsePacketImpl()
    }
}