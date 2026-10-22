package org.softwareanvil.musa.domain.model

/** A free label (theme, fandom, event...) used to group and filter products. */
data class Tag(
	val id: TagId,
	val name: String,
	val color: TagColor,
)

/** Fixed palette tags are painted with; the UI maps each entry to a real colour. */
enum class TagColor { ROSE, AMBER, LIME, TEAL, SKY, VIOLET, SLATE, CORAL }
