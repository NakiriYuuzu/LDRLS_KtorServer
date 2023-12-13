package yuuzu.net.data.model.user

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.Document
import yuuzu.net.utils.Results

class UserDataSourceImpl(
    database: MongoDatabase
): UserDataSource {
    private val collection = database.getCollection<User>(User.TABLE_NAME)

    var x = listOf(User("", "", "", Grade.TEACHER, "", Identity.STUDENT, true, "", "", ""))

    override suspend fun getUserById(id: String): User? {
        return collection.find(
            Filters.eq(User::_id.toString(), id)
        ).firstOrNull()
    }

    override suspend fun getUserByUsername(username: String): User? {
        return collection.find(Document("username", username)).firstOrNull()
    }

    override suspend fun insertUser(user: User): Results<Boolean> {
        return try {
            collection.insertOne(user).wasAcknowledged().let {
                if (it) Results.Success(true)
                else Results.Error("Username already exists")
            }
        } catch (e: Exception) {
            Results.Error(e.message ?: "Unknown error")
        }
    }
}