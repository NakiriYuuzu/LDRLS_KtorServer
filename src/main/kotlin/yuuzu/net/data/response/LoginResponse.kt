package yuuzu.net.data.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String
)