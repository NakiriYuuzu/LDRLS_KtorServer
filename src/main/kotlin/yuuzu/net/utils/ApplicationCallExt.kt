package yuuzu.net.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import yuuzu.net.common.JWTSetup
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.User.Companion.verifyIdentity
import yuuzu.net.data.model.user.UserDataSource

interface Validatable {
    fun isValid(): Pair<Boolean, String>
}

suspend inline fun <reified T : Validatable> ApplicationCall.receiveAndValidate(): Pair<T?, String> {
    val request = kotlin.runCatching { receiveNullable<T>() }.getOrNull()
    request.toString().logd()
    if (request == null) {
        return null to "Please check your request body."
    }

    val (isValid, errorMessage) = request.isValid()
    isValid.toString().logd()
    errorMessage.logd()
    if (!isValid) {
        return null to errorMessage
    }

    return request to ""
}

suspend fun ApplicationCall.verifyJWToken(userDataSource: UserDataSource): Results<User> {
    val id = this.principal<JWTPrincipal>()?.payload?.getClaim(JWTSetup.TOKEN_ID)?.asString()
        ?: return Results.Error("Token is invalid")

    when (val user = userDataSource.getUserById(id)) {
        is Results.Success -> {
            if (user.data == null) return Results.Error("User not found")
            if (!user.data.validator) return Results.Error("User is not validated")
            return Results.Success(user.data)
        }
        is Results.Error -> return Results.Error(user.message)
    }
}

suspend fun ApplicationCall.verifyJWToken(userDataSource: UserDataSource, identity: Identity): Results<User> {
    val id = this.principal<JWTPrincipal>()?.payload?.getClaim(JWTSetup.TOKEN_ID)?.asString()
        ?: return Results.Error("Token is invalid")

    when (val user = userDataSource.getUserById(id)) {
        is Results.Success -> {
            if (user.data == null) return Results.Error("User not found")
            if (!user.data.validator) return Results.Error("User is not validated")
            if (!user.data.verifyIdentity(identity)) return Results.Error("User Permission Denied")
            return Results.Success(user.data)
        }
        is Results.Error -> return Results.Error(user.message)
    }
}