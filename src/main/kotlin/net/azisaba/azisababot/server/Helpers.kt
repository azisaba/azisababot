package net.azisaba.azisababot.server

fun server(block: Server.Builder.() -> Unit): Server = ServerImpl.BuilderImpl().apply(block).build()
