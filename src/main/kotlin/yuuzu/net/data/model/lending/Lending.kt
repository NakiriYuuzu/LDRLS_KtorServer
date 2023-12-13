package yuuzu.net.data.model.lending

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Lending(
    @SerialName("_id") val _id: String = ObjectId.get().toString(),
    val userId: String,
    val roomId: String,
    val adminId: String,
    val startTime: Long,
    val endTime: Long,
    val status: String,
    val createdTime: Long,
    val updatedTime: Long,
) {
    companion object {
        const val TABLE_NAME = "LDRLS_LENDING"
    }
}