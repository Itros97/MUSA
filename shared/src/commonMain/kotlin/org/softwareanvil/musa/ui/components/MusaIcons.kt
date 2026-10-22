package org.softwareanvil.musa.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Material icons missing from material-icons-core, built from their SVG paths. */
object MusaIcons {
	val Remove = icon("Remove", "M19,13H5v-2h14v2z")
	val Physical = icon(
		"Physical",
		"M20,2H4C3,2 2,2.9 2,4v3.01C2,7.73 2.43,8.35 3,8.7V20c0,1.1 1.1,2 2,2h14c0.9,0 2,-0.9 2,-2V8.7c0.57,-0.35 1,-0.97 1,-1.69V4" +
			"C22,2.9 21,2 20,2zM15,14H9v-2h6V14zM20,7H4V4h16V7z",
	)
	val Digital = icon(
		"Digital",
		"M7,14c-1.66,0 -3,1.34 -3,3 0,1.31 -1.16,2 -2,2 0.92,1.22 2.49,2 4,2 2.21,0 4,-1.79 4,-4 0,-1.66 -1.34,-3 -3,-3z" +
			"M20.71,4.63l-1.34,-1.34c-0.39,-0.39 -1.02,-0.39 -1.41,0L9,12.25 11.75,15l8.96,-8.96c0.39,-0.39 0.39,-1.02 0,-1.41z",
	)
	val Archive = icon(
		"Archive",
		"M20.54,5.23l-1.39,-1.68C18.88,3.21 18.47,3 18,3H6c-0.47,0 -0.88,0.21 -1.16,0.55L3.46,5.23C3.17,5.57 3,6.02 3,6.5V19" +
			"c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V6.5c0,-0.48 -0.17,-0.93 -0.46,-1.27zM12,17.5L6.5,12H10v-2h4v2h3.5L12,17.5z" +
			"M5.12,5l0.81,-1h12l0.94,1H5.12z",
	)
	val Unarchive = icon(
		"Unarchive",
		"M20.55,5.22l-1.39,-1.68C18.88,3.21 18.47,3 18,3H6c-0.47,0 -0.88,0.21 -1.15,0.55L3.46,5.22C3.17,5.57 3,6.01 3,6.5V19" +
			"c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V6.5c0,-0.49 -0.17,-0.93 -0.45,-1.28zM12,9.5l5.5,5.5H14v2h-4v-2H6.5L12,9.5z" +
			"M5.12,5l0.82,-1h12l0.93,1H5.12z",
	)
	val Copy = icon(
		"Copy",
		"M16,1H4c-1.1,0 -2,0.9 -2,2v14h2V3h12V1zM19,5H8c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h11c1.1,0 2,-0.9 2,-2V7" +
			"c0,-1.1 -0.9,-2 -2,-2zM19,21H8V7h11v14z",
	)
	val Label = icon(
		"Label",
		"M17.63,5.84C17.27,5.33 16.67,5 16,5L5,5.01C3.9,5.01 3,5.9 3,7v10c0,1.1 0.9,1.99 2,1.99L16,19c0.67,0 1.27,-0.33 1.63,-0.84L22,12l-4.37,-6.16z",
	)

	val Palette = icon(
		"Palette",
		"M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9c0.83,0 1.5,-0.67 1.5,-1.5 0,-0.39 -0.15,-0.74 -0.39,-1.01 -0.23,-0.26 -0.38,-0.61 -0.38,-0.99" +
			" 0,-0.83 0.67,-1.5 1.5,-1.5H16c2.76,0 5,-2.24 5,-5 0,-4.42 -4.03,-8 -9,-8zM6.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,9 6.5,9" +
			" 8,9.67 8,10.5 7.33,12 6.5,12zM9.5,8C8.67,8 8,7.33 8,6.5S8.67,5 9.5,5s1.5,0.67 1.5,1.5S10.33,8 9.5,8zM14.5,8c-0.83,0 -1.5,-0.67" +
			" -1.5,-1.5S13.67,5 14.5,5s1.5,0.67 1.5,1.5S15.33,8 14.5,8zM17.5,12c-0.83,0 -1.5,-0.67 -1.5,-1.5S16.67,9 17.5,9s1.5,0.67 1.5,1.5" +
			" -0.67,1.5 -1.5,1.5z",
	)
	val Event = icon(
		"Event",
		"M17,12h-5v5h5v-5zM16,1v2H8V1H6v2H5c-1.11,0 -1.99,0.9 -1.99,2L3,19c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2" +
			"h-1V1h-2zM19,19H5V8h14v11z",
	)
	val Sales = icon(
		"Sales",
		"M18,6h-2c0,-2.21 -1.79,-4 -4,-4S8,3.79 8,6H6c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2z" +
			"M10,10c0,0.55 -0.45,1 -1,1s-1,-0.45 -1,-1V8h2v2zM12,4c1.1,0 2,0.9 2,2h-4c0,-1.1 0.9,-2 2,-2zM16,10c0,0.55 -0.45,1 -1,1" +
			"s-1,-0.45 -1,-1V8h2v2z",
	)
	val TrendingUp = icon("TrendingUp", "M16,6l2.29,2.29 -4.88,4.88 -4,-4L2,16.59 3.41,18l6,-6 4,4 6.3,-6.29L22,12V6z")

	private fun icon(name: String, path: String) = ImageVector.Builder(
		name = name,
		defaultWidth = 24.dp,
		defaultHeight = 24.dp,
		viewportWidth = 24f,
		viewportHeight = 24f,
	).addPath(pathData = addPathNodes(path), fill = SolidColor(Color.Black)).build()
}
