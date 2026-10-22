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

For iOS, open `iosApp/` in Xcode on a Mac and run it from there.

## Run the tests

```bash
./gradlew :shared:jvmTest            # run the shared tests on the JVM
```

## Where the data lives

The database is a single file called `musa.db`. On desktop it is stored in the per-user data folder of each system:

| System  | Folder                                        |
|---------|-----------------------------------------------|
| Linux   | `$XDG_DATA_HOME/MUSA` or `~/.local/share/MUSA` |
| macOS   | `~/Library/Application Support/MUSA`          |
| Windows | `%APPDATA%\MUSA`                              |

On Android and iOS it lives in the app's private storage.

> ⚠️ The schema still changes without migrations. Delete `musa.db` if the app fails to open after an update.
