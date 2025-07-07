package net.azisaba.azisababot.packet.impl

import net.azisaba.azisababot.packet.Packet
import net.azisaba.azisababot.packet.ServerboundStatusRequestPacket

internal class ServerboundStatusRequestPacketImpl : PacketImpl(), ServerboundStatusRequestPacket {
    override val packetId: Int = 0x00

    override val bound: Packet.Bound = Packet.Bound.SERVER
}