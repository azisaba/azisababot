package net.azisaba.azisababot.client.packet

import java.io.OutputStream

interface PacketWriter {
    fun writeTo(output: OutputStream)
}