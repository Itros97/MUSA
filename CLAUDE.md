# MUSA

Mobile and desktop app for a digital artist who also sells physical stock: inventory, accounts, marketplace, shipping, events and income-tax records. Built module by module, **starting with inventory**.

Always work with the `sdd-code` skill: spec → plan → approval → implementation.

## Decisions already taken

- **Stack**: Kotlin Multiplatform + Compose Multiplatform, SQLDelight for storage. Package `org.softwareanvil.musa`. Created from the KMP Wizard.
- **v1 is fully local**: no server, one device per artist, a single user. Data in a local `.db` file with automatic periodic backups.
- **Later (not v1)**: optional sync with a Kotlin + Spring Boot backend as a modular monolith (not Quarkus). Sync will be last-write-wins with timestamps and client-generated UUIDs, so use UUIDs as ids from the start.
- **Domain**: `Product` with two kinds, `DigitalProduct` and `PhysicalProduct`.
- **Platforms**: develop on Linux; iOS builds on a Mac.

## Architecture

Follow the layering used in PMythMaker: `domain/` (models, use cases, pure logic), `data/` (repositories: interface + `Impl`, SQLDelight queries, mappers), `ui/` (screens + ViewModels with `StateFlow`).
