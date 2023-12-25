package yuuzu.net.data.model.room

import yuuzu.net.utils.Results

interface RoomDataSource {
    suspend fun getRoomById(id: String): Results<Room?>
    suspend fun getRoomByName(name: String): Results<Room?>
    suspend fun getRooms(): Results<List<Room>>
    suspend fun insertRoom(room: Room): Results<Boolean>
    suspend fun updateRoom(room: Room): Results<Boolean>
    suspend fun disableRoom(id: String): Results<Boolean>
}