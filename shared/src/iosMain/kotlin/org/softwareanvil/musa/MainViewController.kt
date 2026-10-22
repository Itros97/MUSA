package org.softwareanvil.musa

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.mp.KoinPlatform
import org.softwareanvil.musa.di.initKoin

/** Entry point called from SwiftUI; starts Koin the first time. */
fun MainViewController() = ComposeUIViewController {
	App()
}.also {
	if (KoinPlatform.getKoinOrNull() == null) initKoin()
}
