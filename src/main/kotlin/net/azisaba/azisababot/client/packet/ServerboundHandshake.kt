package net.azisaba.azisababot.client.packet

import net.azisaba.azisababot.client.data.CustomDataType
import net.azisaba.azisababot.client.data.DataType

object ServerboundHandshake : PacketImpl.TypeImpl() {
    override val packetId: Int = 0x00

    val protocolVersion: Packet.Field<Int> = varInt("Protocol Version")

    val serverAddress: Packet.Field<String> = string("Server Address")

    val serverPort: Packet.Field<UShort> = ushort("Server Port")

    val intent: Packet.Field<Intent> = field("Intent", IntentType)

    enum class Intent(val id: Int) {
        STATUS(1),
        LOGIN(2),
        TRANSFER(3)
    }

    private object IntentType : CustomDataType<Intent, Int> {
        override val primitive: DataType<Int> = DataType.VAR_INT

        override fun toPrimitive(value: Intent): Int = value.id

        override fun fromPrimitive(value: Int): Intent = Intent.entries.first { it.id == value }
    }
}