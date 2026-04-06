package org.softwareanvil.project.domain.usecase

import org.softwareanvil.project.domain.model.Commission
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.domain.repository.CommissionRepository
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

class CreateCommissionUseCase(private val repository: CommissionRepository) {
    suspend operator fun invoke(commission: Commission): Result<Unit> {
        if (commission.clientName.isBlank()) return Result.failure(Exception("El nombre del cliente es obligatorio"))
        if (commission.title.isBlank()) return Result.failure(Exception("El título es obligatorio"))
        if (commission.priceGross <= 0) return Result.failure(Exception("El precio debe ser mayor que 0"))

        val now = Clock.System.now().toEpochMilliseconds()
        repository.insert(commission.copy(createdAt = now))
        return Result.success(Unit)
    }
}

class UpdateCommissionUseCase(private val repository: CommissionRepository) {
    suspend operator fun invoke(commission: Commission): Result<Unit> {
        if (commission.clientName.isBlank()) return Result.failure(Exception("El nombre del cliente es obligatorio"))
        if (commission.title.isBlank()) return Result.failure(Exception("El título es obligatorio"))
        if (commission.priceGross <= 0) return Result.failure(Exception("El precio debe ser mayor que 0"))

        repository.update(commission)
        return Result.success(Unit)
    }
}

class UpdateStatusUseCase(private val repository: CommissionRepository) {
    suspend operator fun invoke(id: Long, currentStatus: CommissionStatus, newStatus: CommissionStatus): Result<Unit> {
        if (!currentStatus.canTransitionTo(newStatus)) {
            return Result.failure(Exception("No se puede pasar de $currentStatus a $newStatus"))
        }

        if (newStatus == CommissionStatus.COMPLETED) {
            val now = Clock.System.now().toEpochMilliseconds()
            repository.markCompleted(id, now)
        } else {
            repository.updateStatus(id, newStatus)
        }
        return Result.success(Unit)
    }
}

class DeleteCommissionUseCase(private val repository: CommissionRepository) {
    suspend operator fun invoke(id: Long, currentStatus: CommissionStatus): Result<Unit> {
        val deletableStatuses = setOf(
            CommissionStatus.INQUIRY,
            CommissionStatus.REJECTED,
            CommissionStatus.CANCELLED
        )
        if (currentStatus !in deletableStatuses) {
            return Result.failure(Exception("Solo se pueden borrar comisiones en estado INQUIRY, REJECTED o CANCELLED"))
        }

        repository.delete(id)
        return Result.success(Unit)
    }
}

class GetCommissionsUseCase(private val repository: CommissionRepository) {
    fun all(): Flow<List<Commission>> = repository.getAll()
    fun byStatus(status: CommissionStatus): Flow<List<Commission>> = repository.getByStatus(status)
    fun upcomingDeadlines(): Flow<List<Commission>> = repository.getUpcomingDeadlines()
    fun search(query: String): Flow<List<Commission>> = repository.search(query)
    suspend fun byId(id: Long): Commission? = repository.getById(id)
}