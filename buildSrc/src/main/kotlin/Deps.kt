object Deps {
    object Koin {
        private const val VERSION = "3.5.2-RC1"
        const val CORE = "io.insert-koin:koin-core:$VERSION"
        const val KTOR = "io.insert-koin:koin-ktor:$VERSION"
        const val LOGGER = "io.insert-koin:koin-logger-slf4j:$VERSION"
    }
    object Mongo {
        private const val VERSION = "4.11.0"
        const val COROUTINE = "org.mongodb:mongodb-driver-kotlin-coroutine:$VERSION"
        const val SYNC = "org.mongodb:mongodb-driver-kotlin-sync:$VERSION"
    }
    object Kotlinx {
        private const val DATETIME_VERSION = "0.4.1"
        const val DATETIME = "org.jetbrains.kotlinx:kotlinx-datetime:${DATETIME_VERSION}"
    }
}