package yuuzu.net

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import yuuzu.net.plugins.*

val config = HoconApplicationConfig(ConfigFactory.load("application.conf"))

fun main() {
    embeddedServer(
        Netty,
        port = config.port,
        host = config.host,
        watchPaths = config.property("ktor.deployment.watch").getList(),
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
//    configureHTTP() /** TODO:// When finish the api then show on web. */
    configureKoin()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
