package org.softwareanvil.musa.demo

import org.softwareanvil.musa.domain.logic.IdGenerator
import org.softwareanvil.musa.domain.model.DigitalDetails
import org.softwareanvil.musa.domain.model.DigitalVariant
import org.softwareanvil.musa.domain.model.Money
import org.softwareanvil.musa.domain.model.PhysicalDetails
import org.softwareanvil.musa.domain.model.PhysicalVariant
import org.softwareanvil.musa.domain.model.ProductDraft
import org.softwareanvil.musa.domain.model.ProductId
import org.softwareanvil.musa.domain.model.ProductStatus
import org.softwareanvil.musa.domain.model.VariantId

/** Made-up catalogue for trying the app: prints, stickers, merch and two commissions, with low and sold-out stock. */
internal class DemoCatalogue(private val ids: IdGenerator) {

	/** Every demo product as a draft ready to save. */
	fun drafts(): List<ProductDraft> = listOf(
		physical("Lámina «Gato astronauta»", listOf("Gatos", "Espacio"), "Papel de algodón 300 g",
			item("A4", 1200, 350, "LAM-GAT-A4", stock = 14, min = 4), item("A3", 2000, 600, "LAM-GAT-A3", stock = 6, min = 2)),
		physical("Lámina «Bosque de niebla»", listOf("Paisaje"), "Papel de algodón 300 g",
			item("A4", 1200, 350, "LAM-BOS-A4", stock = 10), item("A3", 2000, 600, "LAM-BOS-A3", stock = 3, min = 3)),
		physical("Pegatina holográfica «Luna»", listOf("Pegatinas", "Espacio"), "Vinilo holográfico", item("", 300, 60, "PEG-LUNA", stock = 40)),
		physical("Pack de pegatinas «Hongos»", listOf("Pegatinas", "Hongos"), "Vinilo mate", item("", 600, 150, "PEG-HONG", stock = 2, min = 5)),
		physical("Chapa «Rana con sombrero»", listOf("Chapas"), "Metal 38 mm", item("", 250, 70, "CHA-RANA", stock = 25)),
		physical("Tote bag «Gato astronauta»", listOf("Gatos", "Merch"), "Algodón orgánico", item("", 1800, 700, "TOT-GAT", stock = 0, min = 2)),
		physical("Llavero acrílico «Dragón»", listOf("Merch"), "Acrílico 5 cm", item("", 800, 220, "LLA-DRAG", stock = 12)),
		physical("Fanzine «Cuaderno de bocetos»", listOf("Fanzines"), "Grapado A5, 24 páginas", item("", 700, 250, "FAN-BOC", stock = 9),
			status = ProductStatus.DRAFT),
		digital("Retrato digital", listOf("Comisiones"), "PNG 4000 px", 21,
			DigitalVariant(VariantId(ids.next()), "Busto", Money(3500), Money(0)),
			DigitalVariant(VariantId(ids.next()), "Cuerpo entero", Money(7000), Money(0))),
		digital("Icono para redes", listOf("Comisiones"), "PNG 1000 px", 7, DigitalVariant(VariantId(ids.next()), "", Money(2000), Money(0))),
	)

	private fun item(name: String, price: Long, cost: Long, sku: String, stock: Int, min: Int? = null) = PhysicalVariant(
		id = VariantId(ids.next()),
		name = name,
		price = Money(price),
		cost = Money(cost),
		sku = sku,
		stock = stock,
		minStock = min,
		weightGrams = null,
		widthMm = null,
		heightMm = null,
		depthMm = null,
	)

	private fun physical(
		name: String,
		tags: List<String>,
		material: String,
		vararg variants: PhysicalVariant,
		status: ProductStatus = ProductStatus.ACTIVE,
	) = ProductDraft(ProductId(ids.next()), name, description = "", tags, status, PhysicalDetails(material, variants.toList()))

	private fun digital(name: String, tags: List<String>, format: String, days: Int, vararg variants: DigitalVariant) =
		ProductDraft(ProductId(ids.next()), name, description = "", tags, ProductStatus.ACTIVE, DigitalDetails(format, days, variants.toList()))
}
