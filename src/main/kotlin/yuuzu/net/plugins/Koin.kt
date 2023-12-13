package yuuzu.net.plugins

import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import yuuzu.net.di.databaseModule
import yuuzu.net.di.sourceModule

fun Application.configureKoin() {
    install(Koin) {
        // place your modules here
        modules(
            databaseModule,
            sourceModule
        )
    }
}