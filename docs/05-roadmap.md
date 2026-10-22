# Roadmap
---

MUSA is built one module at a time. Inventory comes first; the rest follow in roughly this order.

## Version 1, fully local

- [x] **Inventory**: products, variants, stock, prices and tags.
- [ ] **Backups**: automatic periodic copies of the local database.
- [ ] **Accounts**: income and expenses.
- [ ] **Marketplace**: listings and orders from the places the artist sells.
- [ ] **Shipping**: parcels and their state.
- [ ] **Events**: fairs and conventions, with the stock taken to each.
- [ ] **Income tax**: records needed for the yearly tax return.

## Later

- Optional sync with a Kotlin and Spring Boot backend, built as a modular monolith.
  Conflicts are resolved by last write wins, using timestamps and the UUIDs the app already generates.
