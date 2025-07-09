package net.azisaba.azisababot.client.packet

internal object ServerboundPingRequest : PacketImpl.TypeImpl() {
    override val packetId: Int = 0x01

    val timestamp: Packet.Field<Long> = long("Timestamp")
}