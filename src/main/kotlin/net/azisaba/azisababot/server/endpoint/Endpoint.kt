package net.azisaba.azisababot.server.endpoint

sealed interface Endpoint {
    val host: String

    val port: Int

    companion object {
        fun endpoint(host: String, port: Int): Endpoint = EndpointImpl(host, port)
    }
}

private data class EndpointImpl(
    override val host: String,
    override val port: Int
) : Endpoint