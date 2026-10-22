# Architecture
---

MUSA uses Kotlin Multiplatform with Compose Multiplatform for the UI and SQLDelight for storage.
Almost all the code lives in the `shared` module. The `androidApp`, `desktopApp` and `iosApp`
projects are thin entry points.

## Layers

The shared code under `org.softwareanvil.musa` is split in three layers:

| Layer     | Holds                                                                            |
|-----------|----------------------------------------------------------------------------------|
| `domain/` | Models, repository interfaces, use cases and pure logic such as validation and filtering. |
| `data/`   | SQLDelight queries, mappers between rows and models, and repository implementations. |
| `ui/`     | Compose screens and their ViewModels, which expose state as `StateFlow`.         |

Dependencies point inwards: `ui` and `data` depend on `domain`, and `domain` depends on nothing.

## Wiring

Koin connects the layers in `di/`. Each platform only provides two things: its SQLite driver
and the folder where the database lives. Clocks and id generators are injected, so the domain
logic can be tested with fixed values.

## Ids

Every product, variant and tag has a UUID generated on the device. This keeps the door open to
syncing with a backend later without renumbering anything.
