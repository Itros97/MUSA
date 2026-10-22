package org.softwareanvil.musa.domain.logic

import kotlin.uuid.Uuid

/** Produces unique ids; injected so tests can use predictable ones. */
fun interface IdGenerator {
	fun next(): String
}

/** Client-side random UUIDs, ready for future last-write-wins sync. */
class UuidGenerator : IdGenerator {
	override fun next(): String = Uuid.random().toString()
}
