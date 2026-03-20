package com.marketfinder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
// [deep_linking] start
// import com.marketfinder.core.data.deeplink.DeepLinkHandler
// import org.koin.android.ext.android.inject
// [deep_linking] end
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {

    // [deep_linking] start
    // private val deepLinkHandler: DeepLinkHandler by inject()
    // [deep_linking] end

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // [deep_linking] start
        // handleDeepLink(intent)
        // [deep_linking] end

        setContent {
            Box(
                modifier = Modifier.fillMaxSize().safeDrawingPadding()
            ) {
                App {
                    androidContext(this@MainActivity)
                }
            }
        }
    }

    // [deep_linking] start
    // override fun onNewIntent(intent: Intent) {
    //     super.onNewIntent(intent)
    //     handleDeepLink(intent)
    // }
    // //
    // private fun handleDeepLink(intent: Intent?) {
    //     intent?.data?.toString()?.let { uri ->
    //         deepLinkHandler.handleIncomingUri(uri)
    //     }
    // }
    // [deep_linking] end
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
