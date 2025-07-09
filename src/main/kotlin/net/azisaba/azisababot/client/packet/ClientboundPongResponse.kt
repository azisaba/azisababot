package net.azisaba.azisababot.client.packet

object ClientboundPongResponse : PacketImpl.TypeImpl() {
    override val packetId: Int = 0x01

    val timestamp: Packet.Field<Long> = long("Timestamp")
}