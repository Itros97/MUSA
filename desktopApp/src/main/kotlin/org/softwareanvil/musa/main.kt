package org.softwareanvil.musa

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.runBlocking
import org.softwareanvil.musa.data.db.DemoMode
import org.softwareanvil.musa.di.initKoin
import org.softwareanvil.musa.di.seedDemoData

fun main() {
    initKoin()
    if (DemoMode.isEnabled) runBlocking { seedDemoData() }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = if (DemoMode.isEnabled) "MUSA (demo)" else "MUSA",
            state = rememberWindowState(width = 1100.dp, height = 780.dp),
        ) {
            App()
        }
    }
}
