package net.azisaba.azisababot.packet.impl

import kotlinx.serialization.json.Json
import net.azisaba.azisababot.packet.ClientboundStatusResponsePacket
import net.azisaba.azisababot.packet.Packet

internal class ClientboundStatusResponsePacketImpl : PacketImpl(), ClientboundStatusResponsePacket {
    override val packetId: Int = 0x00

    override val bound: Packet.Bound = Packet.Bound.CLIENT

    override var jsonResponse: ClientboundStatusResponsePacket.JsonResponse?
        get() = get(jsonResponseField)?.let { Json.decodeFromString(it) }
        set(value) = set(jsonResponseField, value?.let { Json.encodeToString(it) })

    private val jsonResponseField: Packet.Field<String> = string(0, "JSON Response")
}