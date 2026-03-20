package com.marketfinder.core.di

import androidx.room.Room
// [deep_linking] start
// import com.marketfinder.core.data.deeplink.DeepLinkHandler
// import com.marketfinder.core.data.deeplink.DefaultDeepLinkHandler
// [deep_linking] end
import com.marketfinder.core.data.local.AppDatabase
import com.marketfinder.core.data.local.AppSettings
// [push_notifications] start
// import com.marketfinder.core.data.notifications.IosPushNotificationService
// import com.marketfinder.core.data.notifications.PushNotificationService
// [push_notifications] end
// [firestore] start
// import com.marketfinder.core.data.firestore.FirestoreService
// import com.marketfinder.core.data.firestore.getFirestoreService
// [firestore] end
// [messaging] start
// import com.marketfinder.core.data.messaging.RealtimeDbService
// import com.marketfinder.core.data.messaging.getRealtimeDbService
// [messaging] end
import com.marketfinder.core.settings.IosAppSettings
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun platformModule() = module {
    single {
        // Get iOS Documents directory (proper location for databases)
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val dbPath = requireNotNull(documentDirectory?.path) + "/study_plan.db"

        Room.databaseBuilder<AppDatabase>(name = dbPath)
            .setDriver(androidx.sqlite.driver.bundled.BundledSQLiteDriver())           // REQUIRED for KMP
            .setQueryCoroutineContext(Dispatchers.IO)   // REQUIRED for async
            .fallbackToDestructiveMigration(true)       // Dev convenience
            .build()
    }

    single {
        Darwin.create()
    }

    single<AppSettings> { IosAppSettings(NSUserDefaults.standardUserDefaults) }

    // [push_notifications] start
    // single<PushNotificationService> { IosPushNotificationService() }
    // [push_notifications] end

    // [deep_linking] start
    // single<DeepLinkHandler> { DefaultDeepLinkHandler() }
    // [deep_linking] end

    // [firestore] start
    // single<FirestoreService> { getFirestoreService() }
    // [firestore] end

    // [messaging] start
    // single<RealtimeDbService> { getRealtimeDbService() }
    // [messaging] end
}
