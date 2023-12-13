package yuuzu.net.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class User(
    @SerialName("_id") val _id: String = ObjectId.get().toString(),
    val name: String,
    val email: String,
    val grade: Grade,
    val phone: String,
    val identity: Identity,
    val validator: Boolean,
    val account: String,
    val password: String,
    val salt: String,
) {
    companion object {
        const val TABLE_NAME = "LDRLS_USER"

        fun User.verifyIdentity(identity: Identity): Boolean {
            return this.identity == identity
        }
    }
}