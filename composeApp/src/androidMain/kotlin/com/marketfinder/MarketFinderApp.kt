package com.marketfinder

import android.app.Application
import co.touchlab.kermit.Logger
// [firebase] start
// import com.google.firebase.FirebaseApp
// [firebase] end

class MarketFinderApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // [firebase] start
        // FirebaseApp.initializeApp(this)
        // [firebase] end

        Logger.withTag("MarketFinderApp").d("onCreate")
    }
}