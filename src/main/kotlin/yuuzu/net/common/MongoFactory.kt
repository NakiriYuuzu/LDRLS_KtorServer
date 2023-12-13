package yuuzu.net.common

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import yuuzu.net.data.model.user.User

object MongoFactory {
    private val uri = System.getenv("MONGO_URI") ?: "mongodb://localhost:27017"
    private var isInitialized = false
    val mongoClient = MongoClient.create(uri)

    fun connectDatabase(): MongoDatabase {
        val database = mongoClient.getDatabase(System.getenv("MONGO_DB") ?: "LDRLS_DB")
        if (!isInitialized) {
            isInitialized = true
            runBlocking { initializeDatabase(database) }

        }
        return database
    }

    private suspend fun initializeDatabase(database: MongoDatabase) {
        // set unique index
        database.getCollection<User>(User.TABLE_NAME).apply {
            createIndex(Indexes.ascending(User::account.name), IndexOptions().unique(true))
            createIndex(Indexes.ascending(User::email.name), IndexOptions().unique(true))
        }
    }
}