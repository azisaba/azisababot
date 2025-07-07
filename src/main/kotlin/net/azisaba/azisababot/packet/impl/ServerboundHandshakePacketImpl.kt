package net.azisaba.azisababot.packet.impl

import net.azisaba.azisababot.packet.Packet
import net.azisaba.azisababot.packet.ServerboundHandshakePacket

internal class ServerboundHandshakePacketImpl : PacketImpl(), ServerboundHandshakePacket {
    override val packetId: Int = 0x00

    override val bound: Packet.Bound = Packet.Bound.SERVER

    override var protocolVersion: Int?
        get() = get(protocolVersionField)
        set(value) = set(protocolVersionField, value)

    override var serverAddress: String?
        get() = get(serverAddressField)
        set(value) = set(serverAddressField, value)

    override var serverPort: Short?
        get() = get(serverPortField)
        set(value) = set(serverPortField, value)

    override var intent: ServerboundHandshakePacket.Intent?
        get() = get(intentField)?.let { ServerboundHandshakePacket.Intent.fromId(it) }
        set(value) = set(intentField, value?.id)

    private val protocolVersionField: Packet.Field<Int> = varInt(0, "Protocol Version")

    private val serverAddressField: Packet.Field<String> = string(1, "Server Address")

    private val serverPortField: Packet.Field<Short> = short(2, "Server Port")

    private val intentField: Packet.Field<Int> = varInt(3, "Intent")
}