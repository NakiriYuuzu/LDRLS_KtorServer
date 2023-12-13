package yuuzu.net.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import yuuzu.net.common.JWTSetup
import yuuzu.net.data.model.user.Grade
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.data.request.LoginRequest
import yuuzu.net.data.request.SignUpRequest
import yuuzu.net.data.response.ApiResponse
import yuuzu.net.data.response.LoginResponse
import yuuzu.net.security.hashing.HashingService
import yuuzu.net.security.hashing.SaltedHash
import yuuzu.net.security.token.TokenClaim
import yuuzu.net.security.token.TokenService
import yuuzu.net.utils.Results

fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("/signup") {
        val request = kotlin.runCatching { call.receiveNullable<SignUpRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, ApiResponse("Body must include ${SignUpRequest.SAMPLE_JSON}"))
            return@post
        }

        val grade = Grade.entries.find { it.ordinal == request.grade } ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, ApiResponse("Grade must be 0 or 1"))
            return@post
        }

        val areFieldsBlank = listOf(
            request.name,
            request.account,
            request.password
        ).any { it.isBlank() }

        if (areFieldsBlank) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse("Please fill in all fields! ${SignUpRequest.SAMPLE_JSON}")
            )
            return@post
        }

        val passwordSalt = hashingService.generateSaltedHash(request.password)
        val user = User(
            name = request.name,
            email = request.email,
            grade = grade.ordinal,
            phone = request.phone,
            identity = Identity.OTHER.ordinal,
            validator = true,
            account = request.account,
            password = passwordSalt.hash,
            salt = passwordSalt.salt
        )

        val wasAcknowledged = userDataSource.insertUser(user)
        wasAcknowledged.let { results ->
            when (results) {
                is Results.Success -> {
                    if (results.data)
                        call.respond(HttpStatusCode.OK, ApiResponse("success", true))
                    else
                        call.respond(HttpStatusCode.Conflict, ApiResponse("Sign up failed."))
                }

                is Results.Error -> {
                    call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                }
            }
        }
    }
}

fun Route.signIn(
    tokenService: TokenService,
    hashingService: HashingService,
    userDataSource: UserDataSource,
) {
    post("/login") {
        val request = kotlin.runCatching { call.receiveNullable<LoginRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, ApiResponse("Body must include ${LoginRequest.SAMPLE_JSON}"))
            return@post
        }

        when (val user = userDataSource.getUserByAccount(request.account)) {
            is Results.Success -> {
                if (user.data == null) {
                    call.respond(HttpStatusCode.Conflict, ApiResponse("User not found."))
                    return@post
                }
                val isValidPassword = hashingService.verify(
                    value = request.password,
                    saltedHash = SaltedHash(
                        hash = user.data.password,
                        salt = user.data.salt
                    )
                )

                if (!isValidPassword) {
                    call.respond(HttpStatusCode.Conflict, ApiResponse("Incorrect password."))
                    return@post
                }

                val token = tokenService.generate(
                    config = JWTSetup.tokenConfig,
                    TokenClaim(
                        name = "id",
                        value = user.data._id
                    )
                )

                call.respond(HttpStatusCode.OK, ApiResponse(LoginResponse(token), true))
            }

            is Results.Error -> {
                call.respond(HttpStatusCode.Conflict, ApiResponse(user.message))
            }
        }
    }
}