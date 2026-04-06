package org.softwareanvil.project.data.mapper

import org.softwareanvil.project.domain.model.Commission
import org.softwareanvil.project.domain.model.CommissionStatus
import org.softwareanvil.project.db.Commission as DbCommission

fun DbCommission.toDomain(): Commission {
    return Commission(
        id = id,
        clientName = clientName,
        title = title,
        description = description,
        status = CommissionStatus.valueOf(status),
        priceGross = priceGross,
        paypalTax = paypalTax,
        tip = tip,
        netPrice = netPrice,
        createdAt = createdAt,
        deadline = deadline,
        completedAt = completedAt,
        tags = tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
        notes = notes
    )
}

fun Commission.tagsToString(): String? {
    return if (tags.isEmpty()) null else tags.joinToString(",")
}