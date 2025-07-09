package net.azisaba.azisababot.crawler

import net.azisaba.azisababot.client.MinecraftClient

interface Crawler {
    val client: MinecraftClient

    fun schedule(cronExpression: String)

    fun unschedule(cronExpression: String)
}