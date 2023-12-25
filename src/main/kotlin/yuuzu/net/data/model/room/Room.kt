package yuuzu.net.data.model.room

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import yuuzu.net.utils.Validatable

@Serializable
data class Room(
    @SerialName("_id") val _id: String = ObjectId.get().toString(),
    val name: String,
    val roomSize: Int,
    val roomType: String,
    val roomAccess: String,
    val validator: Boolean = true,
    val modifierTime: Long = Clock.System.now().toEpochMilliseconds(),
) : Validatable {
    companion object {
        const val TABLE_NAME = "LDRLS_ROOM"
    }

    override fun isValid(): Pair<Boolean, String> {
        if (name.isBlank()) return false to "name is blank"
        if (roomType.isBlank()) return false to "room type is blank"
        if (roomAccess.isBlank()) return false to "room access is blank"
        return true to ""
    }

}
