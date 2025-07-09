package net.azisaba.azisababot.client.packet

import java.io.InputStream

interface PacketReader {
    fun readFrom(input: InputStream)
}