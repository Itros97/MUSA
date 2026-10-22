package org.softwareanvil.musa.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Warm studio palette: terracotta primary, sage tertiary, cream surfaces. */
internal val LightColors = lightColorScheme(
	primary = Color(0xFF9A4532),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFFFFDBD2),
	onPrimaryContainer = Color(0xFF3D0700),
	inversePrimary = Color(0xFFFFB4A2),
	secondary = Color(0xFF77574F),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFFFFDBD2),
	onSecondaryContainer = Color(0xFF2C1510),
	tertiary = Color(0xFF5B6236),
	onTertiary = Color(0xFFFFFFFF),
	tertiaryContainer = Color(0xFFE0E8AF),
	onTertiaryContainer = Color(0xFF191E00),
	background = Color(0xFFFFF8F6),
	onBackground = Color(0xFF231917),
	surface = Color(0xFFFFF8F6),
	onSurface = Color(0xFF231917),
	surfaceVariant = Color(0xFFF5DED8),
	onSurfaceVariant = Color(0xFF53433F),
	surfaceTint = Color(0xFF9A4532),
	inverseSurface = Color(0xFF392E2B),
	inverseOnSurface = Color(0xFFFFEDE8),
	outline = Color(0xFF85736F),
	outlineVariant = Color(0xFFD8C2BC),
	surfaceBright = Color(0xFFFFF8F6),
	surfaceDim = Color(0xFFE8D6D2),
	surfaceContainerLowest = Color(0xFFFFFFFF),
	surfaceContainerLow = Color(0xFFFFF1ED),
	surfaceContainer = Color(0xFFFCEAE5),
	surfaceContainerHigh = Color(0xFFF6E4DF),
	surfaceContainerHighest = Color(0xFFF1DFDA),
)

internal val DarkColors = darkColorScheme(
	primary = Color(0xFFFFB4A2),
	onPrimary = Color(0xFF5E1709),
	primaryContainer = Color(0xFF7B2E1D),
	onPrimaryContainer = Color(0xFFFFDBD2),
	inversePrimary = Color(0xFF9A4532),
	secondary = Color(0xFFE7BDB3),
	onSecondary = Color(0xFF442A23),
	secondaryContainer = Color(0xFF5D3F38),
	onSecondaryContainer = Color(0xFFFFDBD2),
	tertiary = Color(0xFFC4CC95),
	onTertiary = Color(0xFF2D330B),
	tertiaryContainer = Color(0xFF434A20),
	onTertiaryContainer = Color(0xFFE0E8AF),
	background = Color(0xFF1A110F),
	onBackground = Color(0xFFF1DFDA),
	surface = Color(0xFF1A110F),
	onSurface = Color(0xFFF1DFDA),
	surfaceVariant = Color(0xFF53433F),
	onSurfaceVariant = Color(0xFFD8C2BC),
	surfaceTint = Color(0xFFFFB4A2),
	inverseSurface = Color(0xFFF1DFDA),
	inverseOnSurface = Color(0xFF392E2B),
	outline = Color(0xFFA08C87),
	outlineVariant = Color(0xFF53433F),
	surfaceBright = Color(0xFF423734),
	surfaceDim = Color(0xFF1A110F),
	surfaceContainerLowest = Color(0xFF140C0A),
	surfaceContainerLow = Color(0xFF231917),
	surfaceContainer = Color(0xFF271D1B),
	surfaceContainerHigh = Color(0xFF322825),
	surfaceContainerHighest = Color(0xFF3D3230),
)

/** Background and content colour pair for badges and chips. */
data class ToneColors(val container: Color, val content: Color)

internal val LightWarning = ToneColors(Color(0xFFFFE08B), Color(0xFF3D2E00))
internal val DarkWarning = ToneColors(Color(0xFF574500), Color(0xFFFFE08B))
