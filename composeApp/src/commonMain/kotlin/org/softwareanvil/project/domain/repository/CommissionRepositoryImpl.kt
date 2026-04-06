package org.softwareanvil.project.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.softwareanvil.project.data.mapper.toDomain
import org.softwareanvil.project.data.mapper.tagsToString
import org.softwareanvil.project.db.MusaDatabase
import org.softwareanvil.project.domain.model.Commission
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.domain.repository.CommissionRepository

class CommissionRepositoryImpl(
    private val database: MusaDatabase
) : CommissionRepository {

    private val queries = database.commissionQueries

    override fun getAll(): Flow<List<Commission>> {
        return queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getByStatus(status: CommissionStatus): Flow<List<Commission>> {
        return queries.selectByStatus(status.name)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override fun getUpcomingDeadlines(): Flow<List<Commission>> {
        return queries.selectUpcomingDeadlines { id, clientName, title, description, status, priceGross, paypalTax, tip, netPrice, createdAt, deadline, completedAt, tags, notes ->
            org.softwareanvil.project.db.Commission(id, clientName, title, description, status, priceGross, paypalTax, tip, netPrice, createdAt, deadline, completedAt, tags, notes)
        }
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): Commission? {
        return withContext(Dispatchers.Default) {
            queries.selectById(id).executeAsOneOrNull()?.toDomain()
        }
    }

    override suspend fun insert(commission: Commission) {
        withContext(Dispatchers.Default) {
            queries.insert(
                clientName = commission.clientName,
                title = commission.title,
                description = commission.description,
                status = commission.status.name,
                priceGross = commission.priceGross,
                paypalTax = commission.paypalTax,
                tip = commission.tip,
                value = commission.priceGross,
                value_ = commission.tip,
                value__ = commission.paypalTax,
                createdAt = commission.createdAt,
                deadline = commission.deadline,
                completedAt = commission.completedAt,
                tags = commission.tagsToString(),
                notes = commission.notes
            )
        }
    }

    override suspend fun update(commission: Commission) {
        withContext(Dispatchers.Default) {
            queries.update(
                clientName = commission.clientName,
                title = commission.title,
                description = commission.description,
                status = commission.status.name,
                priceGross = commission.priceGross,
                paypalTax = commission.paypalTax,
                tip = commission.tip,
                value = commission.priceGross,
                value_ = commission.tip,
                value__ = commission.paypalTax,
                deadline = commission.deadline,
                completedAt = commission.completedAt,
                tags = commission.tagsToString(),
                notes = commission.notes,
                id = commission.id
            )
        }
    }

    override suspend fun updateStatus(id: Long, status: CommissionStatus) {
        withContext(Dispatchers.Default) {
            queries.updateStatus(status = status.name, id = id)
        }
    }

    override suspend fun markCompleted(id: Long, completedAt: Long) {
        withContext(Dispatchers.Default) {
            queries.markCompleted(completedAt = completedAt, id = id)
        }
    }

    override suspend fun delete(id: Long) {
        withContext(Dispatchers.Default) {
            queries.delete(id)
        }
    }

    override fun search(query: String): Flow<List<Commission>> {
        return queries.searchByText(query, query)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toDomain() } }
    }
}