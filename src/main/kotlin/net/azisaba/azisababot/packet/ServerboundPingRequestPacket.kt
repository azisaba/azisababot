package net.azisaba.azisababot.packet

interface ServerboundPingRequestPacket : Packet {
    var timestamp: Long?
}