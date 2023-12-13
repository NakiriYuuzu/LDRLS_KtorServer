package yuuzu.net.data.request

import kotlinx.serialization.Serializable

@Serializable
data class SignUpRequest(
    val name: String,
    val email: String,
    val grade: Int,
    val phone: String,
    val account: String,
    val password: String
) {
    companion object {
        const val SAMPLE_JSON =
        """
        {
            "name": "",
            "email": "",
            "grade": 0, // 0 = "BACHELOR", 1 = "MASTER"
            "phone": "",
            "account": "",
            "password": ""
        }
        """
    }
}