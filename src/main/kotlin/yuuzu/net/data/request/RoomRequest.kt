package yuuzu.net.data.request

import kotlinx.serialization.Serializable
import yuuzu.net.utils.Validatable

@Serializable
data class RoomRequest(
    val name: String,
    val roomSize: Int,
    val roomType: String,
    val roomAccess: String
) : Validatable {
    override fun isValid(): Pair<Boolean, String> {
        return true to ""
    }
}