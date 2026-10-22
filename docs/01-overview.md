# Overview
---

MUSA is a local-first inventory and business app for digital artists who also sell physical stock.
It runs on Android, desktop and iOS from one Kotlin Multiplatform codebase.

## What it does today

The first module is **inventory**. It keeps the artist's catalogue in a local SQLite database:

- **Products** of two kinds: physical stock (prints, stickers, merch) and digital work made to order (commissions).
- **Variants** for each product, each with its own price and cost. Physical variants also carry SKU, stock, minimum stock, weight and size.
- **Tags** with a colour from a fixed palette, used to group and filter the catalogue.
- **Statuses**: draft, active and archived.

## What it does not do

- It has no server and no account. One device belongs to one artist.
- It does not sync between devices yet. See the [roadmap](05-roadmap.md).

## Read next

- [Get started](02-get-started.md) to build and run it.
- [Architecture](03-architecture.md) to see how the code is laid out.
- [Inventory](04-inventory.md) for the rules the inventory module follows.
