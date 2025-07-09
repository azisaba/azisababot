package net.azisaba.azisababot.client.packet

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.azisaba.azisababot.util.ComponentSerializer
import net.kyori.adventure.text.Component

object ClientboundStatusResponse : PacketImpl.TypeImpl() {
    override val packetId: Int = 0x00

    val jsonResponse: Packet.Field<String> = string("JSON Response")

    private val json: Json = Json { ignoreUnknownKeys = true }

    fun deserialize(jsonResponse: String): JsonResponse = json.decodeFromString(jsonResponse)

    @Serializable
    data class JsonResponse(
        val version: Version,
        val players: Players,
        @Serializable(with = ComponentSerializer::class) val description: Component,
        val favicon: String
    )

    @Serializable
    data class Version(
        val name: String,
        val protocol: Int
    )

    @Serializable
    data class Players(
        val online: Int,
        val max: Int
    )
}