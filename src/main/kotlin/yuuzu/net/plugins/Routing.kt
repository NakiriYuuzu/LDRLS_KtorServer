package yuuzu.net.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.route.createUser
import yuuzu.net.security.hashing.SHA256HashingService
import yuuzu.net.security.token.JwtTokenService
import yuuzu.net.utils.logd
import yuuzu.net.utils.loge
import yuuzu.net.utils.logi
import yuuzu.net.utils.logw

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

    routing {
        get("/") {
            val result = "Nothing here yet!"
            result.logi()
            result.logd()
            result.logw()
            result.loge()
            call.respondText("Nothing here yet!")
        }
        // public image files
        staticResources("images", "static/images")
        createUser(hashingService, userDataSource)
    }
}
