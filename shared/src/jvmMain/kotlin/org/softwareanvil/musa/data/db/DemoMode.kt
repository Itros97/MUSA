package org.softwareanvil.musa.data.db

/** Whether the desktop app runs on the demo database, set with `-Dmusa.demo=true` (see the `runDemo` task). */
object DemoMode {

	val isEnabled: Boolean get() = System.getProperty("musa.demo") == "true"
}
