package net.azisaba.azisababot.client.packet

object ServerboundHandshake : PacketImpl.TypeImpl() {
    override val packetId: Int = 0x00

    val protocolVersion: Packet.Field<Int> = varInt("Protocol Version")

    val serverAddress: Packet.Field<String> = string("Server Address")

    val serverPort: Packet.Field<UShort> = ushort("Server Port")

    val intent: Packet.Field<Int> = varInt("Intent")

    enum class Intent(val id: Int) {
        STATUS(1),
        LOGIN(2),
        TRANSFER(3)
    }
}