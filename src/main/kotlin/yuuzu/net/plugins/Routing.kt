package yuuzu.net.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.java.KoinJavaComponent.inject
import yuuzu.net.data.model.room.RoomDataSource
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.route.room
import yuuzu.net.route.signIn
import yuuzu.net.route.user
import yuuzu.net.security.hashing.SHA256HashingService
import yuuzu.net.security.token.JwtTokenService

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    // service field
    val tokenService = JwtTokenService()
    val hashingService = SHA256HashingService()

    // data source field
    val userDataSource: UserDataSource by inject(UserDataSource::class.java)
    val roomDataSource: RoomDataSource by inject(RoomDataSource::class.java)

    routing {
        get("/") {
            call.respondText("${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}")
        }
        // public image static files
        staticResources("images", "static/images")

        // UserRoute
        user(hashingService, userDataSource)
        signIn(tokenService, hashingService, userDataSource)

        // RoomRoute
        room(roomDataSource,userDataSource)
    }
}
