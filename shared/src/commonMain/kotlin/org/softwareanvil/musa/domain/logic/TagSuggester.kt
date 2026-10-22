package org.softwareanvil.musa.domain.logic

import org.softwareanvil.musa.domain.model.Tag

/** Picks existing tags to suggest while the artist types. */
object TagSuggester {

	const val MAX_SUGGESTIONS = 8

	/** Tags not yet selected whose name contains [query]; with an empty query, any unselected tags. */
	fun suggest(all: List<Tag>, selectedNames: List<String>, query: String): List<Tag> {
		val selected = selectedNames.map(TagNormalizer::key).toSet()
		val typed = TagNormalizer.key(query)
		return all
			.filter { TagNormalizer.key(it.name) !in selected }
			.filter { typed.isEmpty() || TagNormalizer.key(it.name).contains(typed) }
			.take(MAX_SUGGESTIONS)
	}
}
