# Get started
---

## Requirements

- JDK 17 or newer.
- For Android, the Android SDK and a device or emulator.
- For iOS, a Mac with Xcode.

## Run the app

```bash
./gradlew :desktopApp:run            # run the desktop app
./gradlew :androidApp:installDebug   # install on a connected Android device or emulator
```

## Try it with toy data

```bash
./gradlew :desktopApp:runDemo        # run the desktop app on a database full of made-up data
```

The demo uses its own file, `musa-demo.db`, next to the real one. It is deleted and filled again every
time the demo starts, so you can break things freely. It holds ten products (some low on stock, one sold
out), an event that closed last month, one happening today, one coming up, and four orders from other channels. Your real `musa.db` is never
touched.

## Development tools

Development builds show a **Herramientas de prueba** card at the bottom of the home screen: the Android
debug build (`installDebug`) and the desktop app launched from Gradle (`run` or `runDemo`). Release builds
and packaged installers never show it.

- **Rellenar con datos de prueba** loads the same toy data as `runDemo`. It only works on an empty database,
  so the made-up SKUs never clash with real ones.
- **Vaciar base de datos** deletes every product, tag, event, sale and expense after asking for confirmation.
  It really deletes the rows and cannot be undone.

For iOS, open `iosApp/` in Xcode on a Mac and run it from there.

## Run the tests

```bash
./gradlew :shared:jvmTest            # run the shared tests on the JVM
```

## Where the data lives

The database is a single file called `musa.db` (`musa-demo.db` for the demo). On desktop it is stored in the per-user data folder of each system:

| System  | Folder                                        |
|---------|-----------------------------------------------|
| Linux   | `$XDG_DATA_HOME/MUSA` or `~/.local/share/MUSA` |
| macOS   | `~/Library/Application Support/MUSA`          |
| Windows | `%APPDATA%\MUSA`                              |

On Android and iOS it lives in the app's private storage.

> ⚠️ The schema still changes without migrations. Delete `musa.db` if the app fails to open after an update.
