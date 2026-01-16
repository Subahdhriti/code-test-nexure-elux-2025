package io.nexure.discount

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing

const val DISCOUNT_ENDPOINT = "/discount"

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8082,
        host = "0.0.0.0",
        module = Application::routing,
    ).start(true)
}

fun Application.routing() {
    routing {
        // define your routes here
    }
}
