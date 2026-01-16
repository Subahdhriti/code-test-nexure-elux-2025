package io.nexure.discount

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.nexure.discount.database.DatabaseFactory
import io.nexure.discount.plugins.configureRouting
import io.nexure.discount.plugins.configureSerialization

fun main() {
    embeddedServer(
        factory = Netty,
        port = 8082,
        host = "0.0.0.0",
        module = Application::module,
    ).start(true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureRouting()
}
