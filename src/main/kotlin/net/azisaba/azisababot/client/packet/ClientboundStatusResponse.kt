package net.azisaba.azisababot.client.packet

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.azisaba.azisababot.client.data.CustomDataType
import net.azisaba.azisababot.client.data.DataType
import net.azisaba.azisababot.util.ComponentSerializer
import net.kyori.adventure.text.Component

object ClientboundStatusResponse : PacketImpl.TypeImpl() {
    override val packetId: Int = 0x00

    val jsonResponse: Packet.Field<JsonResponse> = field("JSON Response", JsonResponseType)

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

    private object JsonResponseType : CustomDataType<JsonResponse, String> {
        override val primitive: DataType<String> = DataType.STRING

        private val json: Json = Json {
            ignoreUnknownKeys = true
        }

        override fun toPrimitive(value: JsonResponse): String = json.encodeToString(value)

        override fun fromPrimitive(value: String): JsonResponse = json.decodeFromString(value)
    }
}