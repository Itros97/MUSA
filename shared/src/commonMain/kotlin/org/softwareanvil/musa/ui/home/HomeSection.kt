package org.softwareanvil.musa.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.softwareanvil.musa.ui.components.MusaIcons
import org.softwareanvil.musa.ui.theme.MusaColors
import org.softwareanvil.musa.ui.theme.ToneColors

/** Theme colour pair that identifies a section on the home screen. */
enum class SectionTone { PRIMARY, TERTIARY, SECONDARY, WARNING }

/** Areas of the app reachable from the home screen. */
enum class HomeSection(
	val title: String,
	val description: String,
	val icon: ImageVector,
	val tone: SectionTone,
	val isAvailable: Boolean,
) {
	CATALOGUE(
		title = "Catálogo",
		description = "Tus productos digitales y físicos, con variantes, precios y stock.",
		icon = MusaIcons.Palette,
		tone = SectionTone.PRIMARY,
		isAvailable = true,
	),
	EVENTS(
		title = "Eventos",
		description = "Ferias y mercados: qué llevas, cuánto vendes y cuánto te cuesta ir.",
		icon = MusaIcons.Event,
		tone = SectionTone.TERTIARY,
		isAvailable = false,
	),
	SALES(
		title = "Ventas",
		description = "Cada venta registrada, con su canal, su margen y su envío.",
		icon = MusaIcons.Sales,
		tone = SectionTone.SECONDARY,
		isAvailable = false,
	),
	MARKET_ANALYSIS(
		title = "Análisis de mercado",
		description = "Qué se vende mejor, dónde y a qué precio.",
		icon = MusaIcons.TrendingUp,
		tone = SectionTone.WARNING,
		isAvailable = false,
	),
}

/** Container and content colours of this tone in the current theme. */
@Composable
fun SectionTone.colors(): ToneColors {
	val scheme = MaterialTheme.colorScheme
	return when (this) {
		SectionTone.PRIMARY -> ToneColors(scheme.primaryContainer, scheme.onPrimaryContainer)
		SectionTone.TERTIARY -> ToneColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
		SectionTone.SECONDARY -> ToneColors(scheme.secondaryContainer, scheme.onSecondaryContainer)
		SectionTone.WARNING -> MusaColors.extra.warning
	}
}
