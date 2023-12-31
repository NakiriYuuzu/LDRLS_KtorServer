package yuuzu.net.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import yuuzu.net.common.JWTSetup
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.User.Companion.verifyIdentity
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.data.request.LoginRequest
import yuuzu.net.data.response.ApiResponse
import yuuzu.net.data.response.LoginResponse
import yuuzu.net.security.hashing.HashingService
import yuuzu.net.security.hashing.SaltedHash
import yuuzu.net.security.token.TokenClaim
import yuuzu.net.security.token.TokenService
import yuuzu.net.utils.*

fun Route.user(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("/user") {
        val (request, errorMessage) = call.receiveAndValidate<User>()
        if (request == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse(errorMessage))
            return@post
        }

        val userSize = userDataSource.getUsers().let { results ->
            when (results) {
                is Results.Success -> results.data.size
                is Results.Error -> 0
            }
        }

        var newUser = if (userSize == 0) {
            request.copy(
                _id = ObjectId.get().toString(),
                identity = Identity.ADMIN.ordinal,
                validator = true
            )
        } else {
            request.copy(
                _id = ObjectId.get().toString(),
                identity = Identity.OTHER.ordinal,
                validator = true
            )
        }

        val passwordSalt = hashingService.generateSaltedHash(request.password)
        newUser = newUser.copy(
            password = passwordSalt.hash,
            salt = passwordSalt.salt
        )
        val wasAcknowledged = userDataSource.insertUser(newUser)
        wasAcknowledged.let { results ->
            when (results) {
                is Results.Success -> {
                    if (results.data) call.respond(HttpStatusCode.OK, ApiResponse("success", true))
                    else call.respond(HttpStatusCode.Conflict, ApiResponse("Sign up failed."))
                }

                is Results.Error -> {
                    call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                }
            }
        }
    }
    authenticate {
        get("/user") {
            val request = call.request.rawQueryParameters
            when (val result = call.verifyJWToken(userDataSource, Identity.ADMIN)) { /** 驗證Admin身份 */
                is Results.Success -> { /** 開始實現内容 */
                    val id = request["id"]
                    if (id != null) {
                        when (val userResult = userDataSource.getUserById(id)) {
                            is Results.Error -> call.respond(HttpStatusCode.BadRequest, ApiResponse(userResult.message))
                            is Results.Success -> call.respond(HttpStatusCode.OK, ApiResponse(userResult.data, true))
                        }
                    }
                    when (val usersResult = userDataSource.getUsers()) {
                        is Results.Success -> {
                            call.respond(HttpStatusCode.OK, ApiResponse(usersResult.data, true))
                        }

                        is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(usersResult.message))
                    }
                }

                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(result.message))
            }
        }
        put("/user") {
            when (val token = call.verifyJWToken(userDataSource)) {
                is Results.Success -> {
                    val (request, errorMessage) = call.receiveAndValidate<User>()
                    if (request == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse(errorMessage))
                        return@put
                    }

                    when (val user = userDataSource.getUserById(request._id)) { // 取得要修改的帳號資料
                        is Results.Success -> {
                            val modifyUser: User
                            if (user.data == null) {
                                call.respond(HttpStatusCode.Conflict, ApiResponse("User not found."))
                                return@put
                            }
                            if (token.data.verifyIdentity(Identity.ADMIN)) { // 判斷是否是Admin
                                if (token.data._id == request._id) {
                                    val password = hashingService.generateSaltedHash(request.password)
                                    modifyUser = user.data.copy(
                                        name = request.name,
                                        email = request.email,
                                        phone = request.phone,
                                        grade = request.grade,
                                        identity = request.identity,
                                        validator = request.validator,
                                        account = request.account,
                                        password = password.hash,
                                        salt = password.salt
                                    )
                                } else {
                                    modifyUser = user.data.copy(
                                        name = request.name,
                                        email = request.email,
                                        phone = request.phone,
                                        grade = request.grade,
                                        identity = request.identity,
                                        validator = request.validator,
                                        account = request.account
                                    )
                                }
                            } else { // 如果不是Admin則只允許修改自己的資料但不包過權限
                                if (token.data._id != request._id) { // 判斷是否是自己的資料
                                    call.respond(
                                        HttpStatusCode.Conflict, ApiResponse("You can't modify other user's data.")
                                    )
                                    return@put
                                }

                                val password = hashingService.generateSaltedHash(request.password)
                                modifyUser = user.data.copy(
                                    name = request.name,
                                    email = request.email,
                                    phone = request.phone,
                                    grade = request.grade,
                                    account = request.account,
                                    password = password.hash,
                                    salt = password.salt
                                )
                            }

                            val wasAcknowledged = userDataSource.updateUser(modifyUser)
                            wasAcknowledged.let { results ->
                                when (results) {
                                    is Results.Success -> {
                                        if (results.data) call.respond(
                                            HttpStatusCode.OK, ApiResponse("success", true)
                                        )
                                        else call.respond(HttpStatusCode.Conflict, ApiResponse("Update failed."))
                                    }

                                    is Results.Error -> {
                                        call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                                    }
                                }
                            }
                        }

                        is Results.Error -> {
                            call.respond(HttpStatusCode.Conflict, ApiResponse(user.message))
                            return@put
                        }
                    }
                }

                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(token.message))
            }
        }
        delete("/user") {
            when (val token = call.verifyJWToken(userDataSource)) {
                is Results.Success -> {
                    val request = kotlin.runCatching { call.receiveNullable<User>() }.getOrNull() ?: kotlin.run {
                        call.respond(
                            HttpStatusCode.BadRequest, ApiResponse("Please check your request body.")
                        )
                        return@delete
                    }

                    if (token.data.verifyIdentity(Identity.ADMIN)) {
                        if (token.data._id == request._id) {
                            call.respond(HttpStatusCode.Conflict, ApiResponse("You can't delete yourself."))
                            return@delete
                        }

                        val wasAcknowledged = userDataSource.disableUser(request._id)
                        wasAcknowledged.let { results ->
                            when (results) {
                                is Results.Success -> {
                                    if (results.data) call.respond(
                                        HttpStatusCode.OK, ApiResponse("success", true)
                                    )
                                    else call.respond(HttpStatusCode.Conflict, ApiResponse("Delete failed."))
                                }

                                is Results.Error -> {
                                    call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                                }
                            }
                        }
                    } else {
                        call.respond(HttpStatusCode.Conflict, ApiResponse("You can't delete user."))
                    }
                }

                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(token.message))
            }
        }
    }
}

fun Route.signIn(
    tokenService: TokenService,
    hashingService: HashingService,
    userDataSource: UserDataSource,
) {
    authenticate {
        get("/login") {
            when (val token = call.verifyJWToken(userDataSource)) {
                is Results.Success -> {
                    call.respond(HttpStatusCode.OK, ApiResponse(token.data, true))
                }

                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(token.message))
            }
        }
    }
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
                    value = request.password, saltedHash = SaltedHash(
                        hash = user.data.password, salt = user.data.salt
                    )
                )

                if (!isValidPassword) {
                    call.respond(HttpStatusCode.Conflict, ApiResponse("Incorrect password."))
                    return@post
                }

                val token = tokenService.generate(
                    config = JWTSetup.tokenConfig, TokenClaim(
                        name = "id", value = user.data._id
                    )
                )
                call.respond(HttpStatusCode.OK, ApiResponse(
                    LoginResponse(
                        token = token,
                        name = user.data.name,
                        identity = user.data.identity,
                    ), true)
                )
            }

            is Results.Error -> {
                call.respond(HttpStatusCode.Conflict, ApiResponse(user.message))
            }
        }
    }
}