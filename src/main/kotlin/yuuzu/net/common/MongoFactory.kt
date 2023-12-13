package yuuzu.net.common

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import yuuzu.net.config
import yuuzu.net.data.model.lending.Lending
import yuuzu.net.data.model.room.Room
import yuuzu.net.data.model.user.Grade
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User
import yuuzu.net.security.hashing.SHA256HashingService

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
        val collections = database.listCollectionNames().toList()
        if (!collections.contains(User.TABLE_NAME)) {
            val collection = database.getCollection<User>(User.TABLE_NAME)
            val hashingService = SHA256HashingService()
            val passwordSalt = hashingService.generateSaltedHash(System.getenv("SUPER_PASSWORD"))
            collection.insertOne(
                User(
                    name = System.getenv("SUPER_USER"),
                    email = "",
                    grade = Grade.MASTER.ordinal,
                    phone = "",
                    identity = Identity.ADMIN.ordinal,
                    validator = true,
                    account = System.getenv("SUPER_USER"),
                    password = passwordSalt.hash,
                    salt = passwordSalt.salt
                )
            )
        }

        database.getCollection<User>(User.TABLE_NAME).apply {
            createIndex(Indexes.ascending(User::account.name), IndexOptions().unique(true))
            createIndex(Indexes.ascending(User::email.name), IndexOptions().unique(true))
        }
    }
}