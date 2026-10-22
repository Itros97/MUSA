package org.softwareanvil.musa

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.softwareanvil.musa.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MUSA",
            state = rememberWindowState(width = 1100.dp, height = 780.dp),
        ) {
            App()
        }
    }
}
