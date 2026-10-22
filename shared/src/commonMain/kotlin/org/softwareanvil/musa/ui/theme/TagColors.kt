package org.softwareanvil.musa.ui.theme

import androidx.compose.ui.graphics.Color
import org.softwareanvil.musa.domain.model.TagColor

/** Soft pastel tones for tags in light mode. */
internal val LightTagColors = mapOf(
	TagColor.ROSE to ToneColors(Color(0xFFFFD9E2), Color(0xFF5C1130)),
	TagColor.AMBER to ToneColors(Color(0xFFFFE0B2), Color(0xFF4A2800)),
	TagColor.LIME to ToneColors(Color(0xFFDDEFB0), Color(0xFF243600)),
	TagColor.TEAL to ToneColors(Color(0xFFB9EFE6), Color(0xFF00382F)),
	TagColor.SKY to ToneColors(Color(0xFFCDE5FF), Color(0xFF00325A)),
	TagColor.VIOLET to ToneColors(Color(0xFFE9DDFF), Color(0xFF33196B)),
	TagColor.SLATE to ToneColors(Color(0xFFDEE3EA), Color(0xFF29313A)),
	TagColor.CORAL to ToneColors(Color(0xFFFFDAD3), Color(0xFF5A1A0D)),
)

/** Deep tones for tags in dark mode. */
internal val DarkTagColors = mapOf(
	TagColor.ROSE to ToneColors(Color(0xFF6B2340), Color(0xFFFFD9E2)),
	TagColor.AMBER to ToneColors(Color(0xFF5C3A00), Color(0xFFFFE0B2)),
	TagColor.LIME to ToneColors(Color(0xFF35501A), Color(0xFFDDEFB0)),
	TagColor.TEAL to ToneColors(Color(0xFF00504A), Color(0xFFB9EFE6)),
	TagColor.SKY to ToneColors(Color(0xFF004A7A), Color(0xFFCDE5FF)),
	TagColor.VIOLET to ToneColors(Color(0xFF4A3380), Color(0xFFE9DDFF)),
	TagColor.SLATE to ToneColors(Color(0xFF3F4852), Color(0xFFDEE3EA)),
	TagColor.CORAL to ToneColors(Color(0xFF73301F), Color(0xFFFFDAD3)),
)
