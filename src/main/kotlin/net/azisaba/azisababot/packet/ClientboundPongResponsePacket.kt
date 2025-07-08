package net.azisaba.azisababot.packet

interface ClientboundPongResponsePacket : Packet {
    var timestamp: Long?
}