package org.softwareanvil.musa.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.softwareanvil.musa.data.mapper.ProductMapper
import org.softwareanvil.musa.db.MusaDatabase
import org.softwareanvil.musa.domain.logic.TagNormalizer
import org.softwareanvil.musa.domain.model.Tag
import org.softwareanvil.musa.domain.repository.TagRepository

/** SQLDelight-backed [TagRepository]. */
class TagRepositoryImpl(
	private val database: MusaDatabase,
	private val dispatcher: CoroutineDispatcher,
) : TagRepository {

	override fun observeAll(): Flow<List<Tag>> = database.tagQueries
		.selectAll { id, name, _, color, _, _, _ -> ProductMapper.toTag(id, name, color) }
		.asFlow()
		.mapToList(dispatcher)

	override suspend fun findByKey(key: String): Tag? = withContext(dispatcher) {
		database.tagQueries
			.selectByKey(key) { id, name, _, color, _, _, _ -> ProductMapper.toTag(id, name, color) }
			.executeAsOneOrNull()
	}

	override suspend fun insert(tag: Tag, at: Instant) = withContext(dispatcher) {
		val millis = at.toEpochMilliseconds()
		database.tagQueries.insert(tag.id.value, tag.name, TagNormalizer.key(tag.name), tag.color.name, millis, millis)
		Unit
	}
}
