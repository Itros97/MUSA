package org.softwareanvil.project.domain.repository

import org.softwareanvil.project.domain.model.Commission
import org.softwareanvil.project.domain.model.CommissionStatus
import kotlinx.coroutines.flow.Flow

interface CommissionRepository {
    fun getAll(): Flow<List<Commission>>
    fun getByStatus(status: CommissionStatus): Flow<List<Commission>>
    fun getUpcomingDeadlines(): Flow<List<Commission>>
    suspend fun getById(id: Long): Commission?
    suspend fun insert(commission: Commission)
    suspend fun update(commission: Commission)
    suspend fun updateStatus(id: Long, status: CommissionStatus)
    suspend fun markCompleted(id: Long, completedAt: Long)
    suspend fun delete(id: Long)
    fun search(query: String): Flow<List<Commission>>
}