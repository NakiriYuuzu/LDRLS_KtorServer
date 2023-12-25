package yuuzu.net.data.model.room

import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import yuuzu.net.utils.Results
import yuuzu.net.utils.loge

class RoomDataSourceImpl(
    database: MongoDatabase
):RoomDataSource {
    private val collection = database.getCollection<Room>(Room.TABLE_NAME)
    override suspend fun getRoomById(id: String): Results<Room?> {
        collection.find(Filters.eq(Room::_id.name, id)).firstOrNull()?.let {
            return Results.Success(it)
        }
        return Results.Error("Room not found.")
    }

    override suspend fun getRoomByName(name: String): Results<Room?> {
        collection.find(Filters.eq(Room::name.name, name)).firstOrNull()?.let {
            return Results.Success(it)
        }
        return Results.Error("Room not found.")
    }

    override suspend fun getRooms(): Results<List<Room>> {
//        val skip = (page - 1) * limit

        return try {
            collection.find()
//                .skip(skip)
//                .limit(limit)
                .toList()
                .let { Results.Success(it) }
        } catch (e: Exception) {
            Results.Error("Failed to retrieve rooms.").also { e.message?.loge() }
        }
    }

    override suspend fun insertRoom(room: Room): Results<Boolean> {
        return try {
            collection.insertOne(room).wasAcknowledged().let {
                if (it) Results.Success(true) else Results.Error("Insert room failed.")
            }
        } catch (e: MongoWriteException) {
            if (e.error.code == 11000) {
                Results.Error("Room already exists.").also { e.error.message.loge() }
            } else {
                Results.Error(e.message ?: "Unknown error.").also { e.error.message.loge() }
            }
        }
    }

    override suspend fun updateRoom(room: Room): Results<Boolean> {
        return try {
            collection.replaceOne(Filters.eq(Room::_id.name, room._id), room).wasAcknowledged().let {
                if (it) Results.Success(true) else Results.Error("Update room failed")
            }
        } catch (e: MongoWriteException) {
            Results.Error(e.message ?: "Unknown error.").also { e.error.message.loge() }
        }
    }

    override suspend fun disableRoom(id: String): Results<Boolean> {
        return try {
            collection.updateOne(
                Filters.eq(Room::_id.name, id), Updates.set(Room::validator.name, false)
            ).wasAcknowledged().let {
                if (it) Results.Success(true) else Results.Error("Disable room failed")
            }
        } catch (e: MongoWriteException) {
            Results.Error(e.message ?: "Unknown error.".also { e.error.message.loge() }).also { e.error.message.loge() }
        }
    }
}