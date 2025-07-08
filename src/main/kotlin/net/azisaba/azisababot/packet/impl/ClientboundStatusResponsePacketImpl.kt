package net.azisaba.azisababot.packet.impl

import kotlinx.serialization.json.Json
import net.azisaba.azisababot.packet.ClientboundStatusResponsePacket
import net.azisaba.azisababot.packet.Packet

internal class ClientboundStatusResponsePacketImpl : PacketImpl(), ClientboundStatusResponsePacket {
    override val packetId: Int = 0x00

    override val bound: Packet.Bound = Packet.Bound.CLIENT

    override var jsonResponse: ClientboundStatusResponsePacket.JsonResponse?
        get() {
            return get(jsonResponseField)?.let { json.decodeFromString(it) }
        }
        set(value) = set(jsonResponseField, value?.let { json.encodeToString(it) })

    private val jsonResponseField: Packet.Field<String> = string(0, "JSON Response")

    private val json: Json = Json {
        ignoreUnknownKeys = true
    }
}