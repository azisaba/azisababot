package net.azisaba.azisababot.packet

import kotlinx.serialization.Serializable
import net.azisaba.azisababot.util.ComponentSerializer
import net.kyori.adventure.text.Component

interface ClientboundStatusResponsePacket : Packet {
    var jsonResponse: JsonResponse?

    @Serializable
    data class JsonResponse(
        val version: Version,
        val players: Players,
        @Serializable(with = ComponentSerializer::class)
        val description: Component,
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
        val max: Int,
        val sample: List<PlayerSample>? = null
    )

    @Serializable
    data class PlayerSample(
        val id: String,
        val name: String
    )
}