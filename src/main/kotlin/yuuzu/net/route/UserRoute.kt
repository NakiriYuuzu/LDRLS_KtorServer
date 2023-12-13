package yuuzu.net.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import yuuzu.net.data.model.user.Grade
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.data.request.UserRequest
import yuuzu.net.security.hashing.HashingService
import yuuzu.net.security.token.TokenService
import yuuzu.net.utils.Results

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("/signup") {
        val request = kotlin.runCatching { call.receiveNullable<UserRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Body must include ${UserRequest.SAMPLE_JSON}")
            return@post
        }

        val grade = Grade.entries.find { it.ordinal == request.grade } ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, "Grade must be 0 or 1")
            return@post
        }

        val areFieldsBlank = listOf(
            request.name,
            request.account,
            request.password
        ).any { it.isBlank() }

        if (areFieldsBlank) {
            call.respond(HttpStatusCode.BadRequest, "Please fill in all fields! ${UserRequest.SAMPLE_JSON}")
            return@post
        }

        val user = User(
            name = request.name,
            email = request.email,
            grade = grade.ordinal,
            phone = request.phone,
            identity = 0,
            validator = false,
            account = request.account,
            password = request.password,
            salt = ""
        )

        val wasAcknowledged = userDataSource.insertUser(user)
        wasAcknowledged.let { results ->
            when (results) {
                is Results.Success -> {
                    call.respond(HttpStatusCode.OK)
                }
                is Results.Error -> {
                    call.respond(HttpStatusCode.Conflict, results.message)
                }
            }
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
) {

}