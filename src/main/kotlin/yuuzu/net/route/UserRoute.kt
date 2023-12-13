package yuuzu.net.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import yuuzu.net.data.model.user.Grade
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.security.hashing.HashingService

fun Route.createUser(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    get("/user") {
        val saltedHash = hashingService.generateSaltedHash("qwer")
        val user = User(
            name = "yuuzu",
            email = "yuuzu@yuuzu.net",
            grade = Grade.BACHELOR,
            phone = "010-1234-5678",
            identity = Identity.ADMIN,
            validator = true,
            account = "yuuzu",
            password = "qwer",
            salt = saltedHash.salt
        )

        userDataSource.insertUser(user)

        call.respond(HttpStatusCode.OK)
    }
}