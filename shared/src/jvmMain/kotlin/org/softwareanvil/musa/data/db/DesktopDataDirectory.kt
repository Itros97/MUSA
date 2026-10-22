package org.softwareanvil.musa.data.db

import java.io.File

/** Per-user folder where the desktop app keeps its data, following each OS convention. */
internal object DesktopDataDirectory {

	private const val APP_FOLDER = "MUSA"

	/** Returns the folder, creating it if needed. */
	fun resolve(): File {
		val home = System.getProperty("user.home")
		val os = System.getProperty("os.name").lowercase()
		val base = when {
			"mac" in os -> "$home/Library/Application Support"
			"windows" in os -> System.getenv("APPDATA") ?: "$home/AppData/Roaming"
			else -> System.getenv("XDG_DATA_HOME") ?: "$home/.local/share"
		}
		return File(base, APP_FOLDER).apply { check(isDirectory || mkdirs()) { "Cannot create data folder $this" } }
	}
}
