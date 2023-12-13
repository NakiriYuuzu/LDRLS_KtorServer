package yuuzu.net.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import yuuzu.net.common.JWTSetup
import yuuzu.net.config

fun Application.configureSecurity() {
    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = JWTSetup.tokenConfig.audience
    val jwtDomain = config.property("jwt.domain").getString()
    val jwtRealm = config.property("jwt.realm").getString()
    val jwtSecret = JWTSetup.tokenConfig.secret
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
