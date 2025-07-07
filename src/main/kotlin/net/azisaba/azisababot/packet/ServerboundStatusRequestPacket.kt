package net.azisaba.azisababot.packet

import net.azisaba.azisababot.packet.impl.ServerboundStatusRequestPacketImpl

interface ServerboundStatusRequestPacket : Packet {
    companion object {
        fun create(): ServerboundStatusRequestPacket = ServerboundStatusRequestPacketImpl()
    }
}