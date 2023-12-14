package yuuzu.net.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import yuuzu.net.utils.Validatable

@Serializable
data class User(
    @SerialName("_id") val _id: String = ObjectId.get().toString(),
    val name: String,
    val email: String,
    val grade: Int,
    val phone: String,
    val identity: Int = Identity.OTHER.ordinal,
    val validator: Boolean = true,
    val account: String,
    val password: String,
    val salt: String = ""
): Validatable {
    companion object {
        const val TABLE_NAME = "LDRLS_USER"

        fun User.verifyIdentity(identity: Identity): Boolean {
            if (Identity.entries.find { it.ordinal == identity.ordinal } == null) return false
            return this.identity == identity.ordinal
        }

        fun User.verifyGrade(grade: Grade): Boolean {
            if (Grade.entries.find { it.ordinal == grade.ordinal } == null) return false
            return this.grade == grade.ordinal
        }
    }

    override fun isValid(): Pair<Boolean, String> {
        if (name.isBlank()) return false to "name is blank"
        if (email.isBlank()) return false to "email is blank"
        if (!email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) return false to "email format is incorrect"
        if (Grade.entries.find { it.ordinal == grade } == null) return false to "grade value must be 0 ~ ${Grade.entries.size - 1}"
        if (phone.isBlank()) return false to "phone is blank"
        if (phone.length != 10) return false to "phone length is not 10"
        if (phone.toIntOrNull() == null) return false to "phone is not number"
        if (account.isBlank()) return false to "account is blank"
        if (password.isBlank()) return false to "password is blank"
        if (password.length < 7) return false to "password length is less than 7"
        return true to ""
    }
}