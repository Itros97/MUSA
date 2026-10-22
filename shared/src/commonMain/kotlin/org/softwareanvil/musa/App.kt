package org.softwareanvil.musa

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.softwareanvil.musa.ui.navigation.AppNavHost
import org.softwareanvil.musa.ui.theme.MusaTheme

/** Root of the app; Koin must be started by the platform before showing it. */
@Composable
fun App() {
	MusaTheme {
		Surface(color = MaterialTheme.colorScheme.background) {
			AppNavHost()
		}
	}
}
