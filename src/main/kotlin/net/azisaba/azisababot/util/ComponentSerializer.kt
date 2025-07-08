package net.azisaba.azisababot.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

object ComponentSerializer : KSerializer<Component> {
    private val gsonSerializer: GsonComponentSerializer = GsonComponentSerializer.gson()

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Component", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Component) {
        val json = gsonSerializer.serialize(value)
        encoder.encodeString(json)
    }

    override fun deserialize(decoder: Decoder): Component {
        require(decoder is JsonDecoder) { "This deserializer can only be used with JSON format" }
        val element = decoder.decodeJsonElement()
        val json = element.toString()
        return gsonSerializer.deserialize(json)
    }
}