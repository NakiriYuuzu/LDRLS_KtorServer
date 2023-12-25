package yuuzu.net.data.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val name: String,
    val identity: Int
)