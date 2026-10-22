<div align="center">

# MUSA

**A local-first inventory and business app for digital artists who also sell physical stock.**

It keeps digital and physical products, variants, stock, prices and tags in a local SQLite database on the device. It needs no server or account, and runs on Android, desktop and iOS.

![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF?logo=kotlin&logoColor=white)
![Platforms](https://img.shields.io/badge/platforms-Android%20%7C%20Desktop%20%7C%20iOS-blue)
![Status](https://img.shields.io/badge/status-in%20development-orange)
![License](https://img.shields.io/badge/license-MIT-green)

</div>

> **Note:** written with an AI assistant and pending thorough review. See the [AI disclosure](#-ai-disclosure).

## 🤖 AI disclosure

I write most of my code by hand, so I want to be clear about this project: **it was written by an
AI assistant (Claude, by Anthropic) under my direction, in October 2026.** I decided what to build
and how it should behave; the AI wrote the code, the tests and this README. Every commit made this
way carries a `Co-Authored-By: Claude` line.

**Status: pending thorough review.** The shared module and the desktop app compile, and the shared
JVM tests pass. The iOS build has not been tried yet. I have not yet reviewed it line by line.
Treat it as a working draft, not as code I vouch for.

> ⚠️ **Warning:** in early development, **not suitable for production use**. The database schema will change without migrations.

---

## 🚀 Quick start

Requires JDK 17+ and, for Android, the Android SDK. iOS builds need a Mac with Xcode.

```bash
./gradlew :desktopApp:run            # run the desktop app
./gradlew :androidApp:installDebug   # install on a connected Android device or emulator
./gradlew :shared:jvmTest            # run the shared tests on the JVM
```

For iOS, open `iosApp/` in Xcode on a Mac and run it from there.

[More on getting started →](docs/02-get-started.md)

## 🧭 What is in it

The app is built module by module. The first one is **inventory**:

- Digital and physical products, with variants, stock, cost and sale price.
- Tags with suggestions, search and filters on the product list.
- Validation of product drafts before saving, and duplication of existing products.

Accounts, marketplace, shipping, events and income-tax records will follow as separate modules.
Data stays in a local `.db` file. Ids are UUIDs so that optional sync with a backend can be added later.

## 🔍 How it works

Kotlin Multiplatform with Compose Multiplatform for the UI and SQLDelight for storage. The shared
module is split in three layers: `domain/` holds models, use cases and pure logic; `data/` holds
the SQLDelight queries, mappers and repository implementations; `ui/` holds the screens and their
ViewModels, which expose state as `StateFlow`. Koin wires the layers together, and each platform
only provides its database driver and data directory.

[More on the design →](docs/03-architecture.md)

## 📚 Documentation

| | |
|---|---|
| [Overview](docs/01-overview.md) | What MUSA does today and what it does not do. |
| [Get started](docs/02-get-started.md) | Build, run and test it, and where the data is stored. |
| [Architecture](docs/03-architecture.md) | The domain, data and ui layers and how they are wired. |
| [Inventory](docs/04-inventory.md) | Products, variants, validation rules, filters and duplication. |
| [Roadmap](docs/05-roadmap.md) | The modules still to come and the plan for sync. |

[Read the docs online →](https://itros97.github.io/MUSA/)

## License

MIT, see [LICENSE](LICENSE).
