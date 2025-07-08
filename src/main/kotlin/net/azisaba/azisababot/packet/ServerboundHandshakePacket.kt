package net.azisaba.azisababot.packet

interface ServerboundHandshakePacket : Packet {
    var protocolVersion: Int?

    var serverAddress: String?

    var serverPort: Short?

    var intent: Intent?

    enum class Intent(var id: Int) {
        STATUS(1),
        LOGIN(2),
        TRANSFER(3);

        companion object {
            fun fromId(id: Int): Intent = entries.first { id == it.id }
        }
    }
}