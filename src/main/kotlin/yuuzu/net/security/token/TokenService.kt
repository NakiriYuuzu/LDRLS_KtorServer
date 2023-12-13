package yuuzu.net.security.token

interface TokenService {
    fun generate(
        config: TokenConfig,
        vararg claims: TokenClaim
    ): String
}