package com.marketfinder.core.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
// [deep_linking] start
// import com.marketfinder.core.data.deeplink.DeepLinkHandler
// import com.marketfinder.core.data.deeplink.DefaultDeepLinkHandler
// [deep_linking] end
import com.marketfinder.core.data.local.AppDatabase
import com.marketfinder.core.data.local.AppSettings
// [push_notifications] start
// import com.marketfinder.core.data.notifications.AndroidPushNotificationService
// import com.marketfinder.core.data.notifications.PushNotificationService
// [push_notifications] end
// [firestore] start
// import com.marketfinder.core.data.firestore.FirestoreService
// import com.marketfinder.core.data.firestore.AndroidFirestoreService
// [firestore] end
// [messaging] start
// import com.marketfinder.core.data.messaging.RealtimeDbService
// import com.marketfinder.core.data.messaging.AndroidRealtimeDbService
// [messaging] end
import com.marketfinder.core.settings.AndroidAppSettings
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule() = module {
    // Database
    single {
        val appContext = androidContext().applicationContext
        val dbFile = appContext.getDatabasePath("study_plan.db")
        Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        OkHttp.create {
            preconfigured = getOkHttpClient()
        }
    }

    single<AppSettings> { AndroidAppSettings(androidContext()) }

    // [push_notifications] start
    // single<PushNotificationService> {
    //     AndroidPushNotificationService(androidContext()).also {
    //         AndroidPushNotificationService.instance = it
    //     }
    // }
    // [push_notifications] end

    // [deep_linking] start
    // single<DeepLinkHandler> { DefaultDeepLinkHandler() }
    // [deep_linking] end

    // [firestore] start
    // single<FirestoreService> { AndroidFirestoreService() }
    // [firestore] end

    // [messaging] start
    // single<RealtimeDbService> { AndroidRealtimeDbService() }
    // [messaging] end
}

private fun getOkHttpClient() = OkHttpClient.Builder().build()
