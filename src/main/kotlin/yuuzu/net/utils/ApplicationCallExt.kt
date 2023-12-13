package yuuzu.net.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import yuuzu.net.common.JWTSetup
import yuuzu.net.data.model.user.User
import yuuzu.net.data.model.user.UserDataSource

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