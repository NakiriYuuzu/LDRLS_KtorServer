package yuuzu.net.data.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val account: String,
    val password: String
) {
    companion object {
        const val SAMPLE_JSON =
        """
        {
            "account": "",
            "password": ""
        }
        """
    }
}
