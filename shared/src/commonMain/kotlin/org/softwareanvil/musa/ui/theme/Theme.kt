package org.softwareanvil.musa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.softwareanvil.musa.domain.model.TagColor

/** Colours MUSA needs on top of the Material scheme. */
data class MusaExtraColors(val warning: ToneColors, val tags: Map<TagColor, ToneColors>)

private val LocalExtraColors = staticCompositionLocalOf { MusaExtraColors(LightWarning, LightTagColors) }

private val MusaShapes = Shapes(
	extraSmall = RoundedCornerShape(8.dp),
	small = RoundedCornerShape(12.dp),
	medium = RoundedCornerShape(16.dp),
	large = RoundedCornerShape(24.dp),
	extraLarge = RoundedCornerShape(32.dp),
)

private val MusaTypography = Typography().run {
	copy(
		headlineMedium = headlineMedium.copy(fontWeight = FontWeight.SemiBold),
		headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
		titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
		titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
	)
}

/** App theme; follows the system light/dark setting. */
@Composable
fun MusaTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
	val extra = if (dark) MusaExtraColors(DarkWarning, DarkTagColors) else MusaExtraColors(LightWarning, LightTagColors)
	CompositionLocalProvider(LocalExtraColors provides extra) {
		MaterialTheme(
			colorScheme = if (dark) DarkColors else LightColors,
			shapes = MusaShapes,
			typography = MusaTypography,
			content = content,
		)
	}
}

/** Access to [MusaExtraColors] from any composable. */
object MusaColors {
	val extra: MusaExtraColors
		@Composable @ReadOnlyComposable get() = LocalExtraColors.current
}
