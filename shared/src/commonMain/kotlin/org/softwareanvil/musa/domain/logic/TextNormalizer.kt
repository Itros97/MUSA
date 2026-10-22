package org.softwareanvil.musa.domain.logic

/** Folds text so comparisons ignore case and Spanish/European accents ("Lámina" == "lamina"). */
object TextNormalizer {

	private val ACCENTS = mapOf(
		'á' to 'a', 'à' to 'a', 'ä' to 'a', 'â' to 'a', 'ã' to 'a',
		'é' to 'e', 'è' to 'e', 'ë' to 'e', 'ê' to 'e',
		'í' to 'i', 'ì' to 'i', 'ï' to 'i', 'î' to 'i',
		'ó' to 'o', 'ò' to 'o', 'ö' to 'o', 'ô' to 'o', 'õ' to 'o',
		'ú' to 'u', 'ù' to 'u', 'ü' to 'u', 'û' to 'u',
		'ñ' to 'n', 'ç' to 'c',
	)

	/** Lowercases the text and strips accents. */
	fun fold(text: String): String = buildString(text.length) {
		text.lowercase().forEach { append(ACCENTS[it] ?: it) }
	}
}
