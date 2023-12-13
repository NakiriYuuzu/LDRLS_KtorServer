package yuuzu.net.data.model.user

import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import yuuzu.net.utils.Results
import yuuzu.net.utils.loge

class UserDataSourceImpl(
    database: MongoDatabase
): UserDataSource {
    private val collection = database.getCollection<User>(User.TABLE_NAME)

    override suspend fun getUserById(id: String): Results<User?> {
        collection.find(Filters.eq(User::_id.name, id)).firstOrNull()?.let {
            return Results.Success(it)
        }
        return Results.Error("User not found.")
    }

    override suspend fun getUserByName(name: String): Results<User?> {
        collection.find(Filters.eq(User::name.name, name)).firstOrNull()?.let {
            return Results.Success(it)
        }
        return Results.Error("User not found.")
    }

    override suspend fun getUserByAccount(account: String): Results<User?> {
        collection.find(Filters.eq(User::account.name, account)).firstOrNull()?.let {
            return Results.Success(it)
        }
        return Results.Error("User not found.")
    }

    override suspend fun getUsers(page: Int, limit: Int): Results<List<User>> {
        val skip = (page - 1) * limit

        // 如果要只要顯示特定的欄位，可以使用 projection ex: .projection(Projections.include(User::name.name, User::...))
        return try {
            collection.find()
                .skip(skip)
                .limit(limit)
                .toList()
                .let { Results.Success(it) }
        } catch (e: Exception) {
            // 適當的錯誤處理
            Results.Error("Failed to retrieve users.")
        }
    }


    override suspend fun insertUser(user: User): Results<Boolean> {
        return try {
            collection.insertOne(user).wasAcknowledged().let {
                if (it) Results.Success(true) else Results.Error("Insert user failed.")
            }
        } catch (e: MongoWriteException) {
            if (e.error.code == 11000) {
                Results.Error("User already exists. ${e.message}")
            } else {
                Results.Error(e.message ?: "Unknown error.".also { e.error.message.loge() })
            }
        }
    }

    override suspend fun updateUser(user: User): Results<Boolean> {
        return try {
            collection.replaceOne(Filters.eq(User::_id.name, user._id), user).wasAcknowledged().let {
                if (it) Results.Success(true) else Results.Error("Update user failed.")
            }
        } catch (e: MongoWriteException) {
            Results.Error(e.message ?: "Unknown error.".also { e.error.message.loge() })
        }
    }

    override suspend fun disableUser(user: User): Results<Boolean> {
        return try {
            val newUser = user.copy(validator = false)
            collection.replaceOne(Filters.eq(User::_id.name, user._id), newUser).wasAcknowledged().let {
                if (it) Results.Success(true) else Results.Error("Disable user failed.")
            }
        } catch (e: MongoWriteException) {
            Results.Error(e.message ?: "Unknown error.".also { e.error.message.loge() })
        }
    }
}