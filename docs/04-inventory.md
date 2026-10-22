# Inventory
---

The inventory module keeps the catalogue: what the artist sells, in which versions, at what price and how much is left.

## Products and variants

A product is either **physical** or **digital**, and always has at least one variant.

- A **physical product** has a material, and its variants carry SKU, stock, minimum stock, weight and width, height and depth.
- A **digital product** has a delivery format and an optional turnaround in days. Its variants are levels or formats, such as "sketch" or "full colour".

Each variant has its own price and cost. Money is stored as whole cents to avoid rounding errors.
The margin is price minus cost, also shown as a percentage of the price.

## Rules on save

A draft is checked before it is saved. It is rejected when:

- The product name is blank, or it has no variants.
- There are two or more variants and one has no name, or two share a name (case and accents ignored).
- A price, cost, stock, minimum stock or turnaround is negative.
- A weight or size is zero or negative.
- A SKU repeats, inside the product or in another product.

## Low stock

A physical variant is low on stock when it is sold out, or when its stock is at or below its minimum.
A product is low on stock when any of its variants is.

## Searching and filtering

The product list shows active and draft products, or only archived ones. It can be narrowed by:

- Text, matched against name, description and SKUs, ignoring case and accents.
- Kind (physical or digital).
- Tags. A product must have every selected tag.
- Low stock only.

Results are sorted by name.

## Duplicating

Duplicating a product creates a new draft with new ids. On physical products the stock is reset
to zero and SKUs are cleared, because a SKU must be unique.
