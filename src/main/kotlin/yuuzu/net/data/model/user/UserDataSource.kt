package yuuzu.net.data.model.user

import yuuzu.net.utils.Results

interface UserDataSource {
    suspend fun getUserById(id: String): Results<User?>
    suspend fun getUserByName(name: String): Results<User?>
    suspend fun getUserByAccount(account: String): Results<User?>
    suspend fun getUsers(page: Int, limit: Int): Results<List<User>>
    suspend fun insertUser(user: User): Results<Boolean>
    suspend fun updateUser(user: User): Results<Boolean>
    suspend fun disableUser(id: String): Results<Boolean>
}