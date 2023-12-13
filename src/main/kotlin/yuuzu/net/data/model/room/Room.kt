package yuuzu.net.data.model.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Room(
    @SerialName("_id") val _id: String = ObjectId.get().toString(),
    val name: String,
    val roomSize: Int,
    val roomType: String,
    val roomAccess: String,
    val createdTime: Long,
    val updatedTime: Long
) {
    companion object {
        const val TABLE_NAME = "LDRLS_ROOM"
    }
}
