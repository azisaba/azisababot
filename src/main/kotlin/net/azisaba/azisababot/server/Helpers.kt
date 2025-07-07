package net.azisaba.azisababot.server

import net.azisaba.azisababot.server.impl.ServerImpl

fun server(block: Server.Builder.() -> Unit): Server = ServerImpl.BuilderImpl().apply(block).build()
