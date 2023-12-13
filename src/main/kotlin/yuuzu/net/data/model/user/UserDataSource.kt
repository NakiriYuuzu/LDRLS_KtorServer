package yuuzu.net.data.model.user

import yuuzu.net.utils.Results

interface UserDataSource {
    suspend fun getUserById(id: String): User?
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(user: User): Results<Boolean>
}