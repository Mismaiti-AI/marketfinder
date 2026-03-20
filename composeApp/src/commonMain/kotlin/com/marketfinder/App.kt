package com.marketfinder

import androidx.compose.runtime.*
import com.marketfinder.di.moduleList
import com.marketfinder.presentation.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.KoinAppDeclaration


@Composable
fun App(koinAppDeclaration: KoinAppDeclaration? = null) {
    KoinApplication(application = {
        modules(moduleList())
        koinAppDeclaration?.invoke(this)
    }) {
        AppTheme {
            AppContent()
        }
    }
}
