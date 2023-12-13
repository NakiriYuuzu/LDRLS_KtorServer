package yuuzu.net.data.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val result: T,
    val success: Boolean = false,
)
