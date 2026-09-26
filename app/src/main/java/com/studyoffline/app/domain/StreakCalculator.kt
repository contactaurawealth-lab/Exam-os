package com.studyoffline.app.domain

import com.studyoffline.app.data.model.StudySession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val studiedToday: Boolean
)

object StreakCalculator {
    fun calculate(
        sessions: List<StudySession>,
        zoneId: ZoneId = ZoneId.systemDefault(),
        today: LocalDate = LocalDate.now(zoneId)
    ): StreakInfo {
        val qualifyingDates = sessions
            .filter { it.durationMinutes >= 5 }
            .map { session ->
                Instant.ofEpochMilli(session.startedAt)
                    .atZone(zoneId)
                    .toLocalDate()
            }
            .toSet()

        if (qualifyingDates.isEmpty()) {
            return StreakInfo(currentStreak = 0, longestStreak = 0, studiedToday = false)
        }

        val studiedToday = qualifyingDates.contains(today)
        val studiedYesterday = qualifyingDates.contains(today.minusDays(1))

        // Calculate current streak
        var currentStreak = 0
        var checkDate: LocalDate? = if (studiedToday) today else if (studiedYesterday) today.minusDays(1) else null

        while (checkDate != null && qualifyingDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Calculate longest streak across history
        val sortedDates = qualifyingDates.sorted()
        var longest = 0
        var runningCount = 0
        var prevDate: LocalDate? = null

        for (date in sortedDates) {
            if (prevDate == null) {
                runningCount = 1
            } else {
                if (date.minusDays(1) == prevDate) {
                    runningCount++
                } else {
                    runningCount = 1
                }
            }
            if (runningCount > longest) {
                longest = runningCount
            }
            prevDate = date
        }

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = maxOf(longest, currentStreak),
            studiedToday = studiedToday
        )
    }
}
