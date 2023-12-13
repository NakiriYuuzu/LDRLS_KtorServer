package yuuzu.net.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)