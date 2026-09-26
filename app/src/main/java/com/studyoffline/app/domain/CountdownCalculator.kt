package com.studyoffline.app.domain

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil

object CountdownCalculator {
    fun calculateDaysRemaining(
        goalDate: Long?,
        zoneId: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zoneId)
    ): Long? {
        if (goalDate == null || goalDate <= 0) return null
        val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val diff = goalDate - todayStart
        if (diff <= 0) return 0L
        return ceil(diff.toDouble() / 86_400_000.0).toLong()
    }
}
