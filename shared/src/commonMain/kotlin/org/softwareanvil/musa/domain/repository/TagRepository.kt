package org.softwareanvil.musa.domain.repository

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import org.softwareanvil.musa.domain.model.Tag

/** Storage for tags. */
interface TagRepository {

	/** Emits every tag sorted by name. */
	fun observeAll(): Flow<List<Tag>>

	/** Returns the tag whose normalized key is [key], or null. */
	suspend fun findByKey(key: String): Tag?

	/** Stores a new tag. */
	suspend fun insert(tag: Tag, at: Instant)
}
