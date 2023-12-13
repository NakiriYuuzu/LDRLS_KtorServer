package yuuzu.net.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import yuuzu.net.common.JWTSetup
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.User.Companion.isValid
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.data.request.LoginRequest
import yuuzu.net.data.response.ApiResponse
import yuuzu.net.data.response.LoginResponse
import yuuzu.net.security.hashing.HashingService
import yuuzu.net.security.hashing.SaltedHash
import yuuzu.net.security.token.TokenClaim
import yuuzu.net.security.token.TokenService
import yuuzu.net.utils.Results
import yuuzu.net.utils.verifyJWToken

fun Route.user(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("/user") {
        val request = kotlin.runCatching { call.receiveNullable<User>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest, ApiResponse("Please check your request body."))
            return@post
        }

        val (isValid, errorMessage) = request.isValid()
        if (!isValid) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(errorMessage))
            return@post
        }

        val passwordSalt = hashingService.generateSaltedHash(request.password)
        val user = request.copy(
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
    authenticate {
        put("/user") {
            when (val result = call.verifyJWToken(userDataSource)) {
                is Results.Success -> {
                    val request =
                        kotlin.runCatching { call.receiveNullable<User>() }.getOrNull() ?: kotlin.run {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse("Please check your request body.")
                            )
                            return@put
                        }


                    when (result.data.identity) {
                        Identity.ADMIN.ordinal -> {

                        }
                        else -> {
                            call.respond(HttpStatusCode.Conflict, ApiResponse("Permission denied."))
                        }
                    }
                }
                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(result.message))
            }
        }
        delete("/user") {
            when (val result = call.verifyJWToken(userDataSource)) {
                is Results.Success -> {
                    val request =
                        kotlin.runCatching { call.receiveNullable<User>() }.getOrNull() ?: kotlin.run {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ApiResponse("Please check your request body.")
                            )
                            return@delete
                        }
                }
                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(result.message))
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
            call.respond(HttpStatusCode.BadRequest, ApiResponse("Please check your request body."))
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