package yuuzu.net.di

import org.koin.dsl.module
import yuuzu.net.common.MongoFactory
import yuuzu.net.data.model.room.RoomDataSource
import yuuzu.net.data.model.room.RoomDataSourceImpl
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.data.model.user.UserDataSourceImpl

val databaseModule = module {
    single { MongoFactory.connectDatabase() }
}

val sourceModule = module {
    single<UserDataSource> { UserDataSourceImpl(get()) }
    single<RoomDataSource> { RoomDataSourceImpl(get()) }
}