package com.studyoffline.app

import com.studyoffline.app.domain.CountdownCalculator
import com.studyoffline.app.domain.Sm2ReviewResult
import com.studyoffline.app.domain.SpacedRepetition
import com.studyoffline.app.domain.StreakCalculator
import com.studyoffline.app.data.model.SessionType
import com.studyoffline.app.data.model.StudySession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DomainLogicTest {

    // ----------------------------------------------------
    // Spaced Repetition (SM-2) Tests (PRD §7.2)
    // ----------------------------------------------------
    @Test
    fun sm2_grade0_again_resetsRepetitionsAndInterval() {
        val result = SpacedRepetition.calculateNextReview(
            currentEase = 2.5f,
            currentInterval = 10,
            currentRepetitions = 4,
            grade = 0
        )
        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
        // easeFactor = max(1.3, 2.5 + (0.1 - 3 * (0.08 + 3 * 0.02))) = 2.5 + (0.1 - 0.42) = 2.18
        assertTrue(result.easeFactor < 2.5f)
        assertTrue(result.easeFactor >= 1.3f)
    }

    @Test
    fun sm2_grade3_easy_advancesIntervalAndBoostsEase() {
        // First repetition
        val rep1 = SpacedRepetition.calculateNextReview(
            currentEase = 2.5f,
            currentInterval = 0,
            currentRepetitions = 0,
            grade = 3
        )
        assertEquals(1, rep1.repetitions)
        assertEquals(1, rep1.intervalDays)
        assertEquals(2.6f, rep1.easeFactor, 0.01f)

        // Second repetition
        val rep2 = SpacedRepetition.calculateNextReview(
            currentEase = rep1.easeFactor,
            currentInterval = rep1.intervalDays,
            currentRepetitions = rep1.repetitions,
            grade = 3
        )
        assertEquals(2, rep2.repetitions)
        assertEquals(6, rep2.intervalDays)

        // Third repetition: round(6 * 2.7) = 16
        val rep3 = SpacedRepetition.calculateNextReview(
            currentEase = rep2.easeFactor,
            currentInterval = rep2.intervalDays,
            currentRepetitions = rep2.repetitions,
            grade = 3
        )
        assertEquals(3, rep3.repetitions)
        assertTrue(rep3.intervalDays >= 16)
    }

    @Test
    fun sm2_easeFactor_neverDropsBelowMinimum() {
        var ease = 1.4f
        var interval = 1
        var reps = 1

        // Repeatedly fail
        repeat(10) {
            val res = SpacedRepetition.calculateNextReview(
                currentEase = ease,
                currentInterval = interval,
                currentRepetitions = reps,
                grade = 0
            )
            ease = res.easeFactor
            interval = res.intervalDays
            reps = res.repetitions
        }

        assertEquals(1.3f, ease, 0.001f)
    }

    // ----------------------------------------------------
    // Streak Calculator Tests (PRD §7.1)
    // ----------------------------------------------------
    @Test
    fun streak_emptySessions_returnsZero() {
        val result = StreakCalculator.calculate(emptyList())
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
        assertEquals(false, result.studiedToday)
    }

    @Test
    fun streak_sessionsUnder5Min_doNotCount() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val todayMs = today.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L

        val shortSession = StudySession(
            id = 1,
            startedAt = todayMs,
            durationMinutes = 4, // < 5 min
            type = SessionType.FREE
        )

        val result = StreakCalculator.calculate(listOf(shortSession), zone, today)
        assertEquals(0, result.currentStreak)
        assertEquals(false, result.studiedToday)
    }

    @Test
    fun streak_gracePeriod_yesterdayStreakHeldUntilMidnight() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)
        val yesterdayMs = yesterday.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000L

        val session = StudySession(
            id = 1,
            startedAt = yesterdayMs,
            durationMinutes = 15,
            type = SessionType.POMODORO
        )

        val result = StreakCalculator.calculate(listOf(session), zone, today)
        // Haven't studied today yet, but grace period shows 1
        assertEquals(1, result.currentStreak)
        assertEquals(false, result.studiedToday)
    }

    @Test
    fun streak_consecutiveDays_accumulatesCorrectly() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val sessions = (0..3).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            StudySession(
                id = daysAgo.toLong() + 1,
                startedAt = date.atStartOfDay(zone).toInstant().toEpochMilli() + 1000000L,
                durationMinutes = 25,
                type = SessionType.POMODORO
            )
        }

        val result = StreakCalculator.calculate(sessions, zone, today)
        assertEquals(4, result.currentStreak)
        assertEquals(4, result.longestStreak)
        assertEquals(true, result.studiedToday)
    }

    // ----------------------------------------------------
    // Countdown Calculator Tests (PRD §7.4)
    // ----------------------------------------------------
    @Test
    fun countdown_nullDate_returnsNull() {
        assertNull(CountdownCalculator.calculateDaysRemaining(null))
        assertNull(CountdownCalculator.calculateDaysRemaining(-1L))
    }

    @Test
    fun countdown_futureDate_computesCorrectDays() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val tenDaysFuture = today.plusDays(10).atStartOfDay(zone).toInstant().toEpochMilli()

        val days = CountdownCalculator.calculateDaysRemaining(tenDaysFuture, zone, today)
        assertEquals(10L, days)
    }

    @Test
    fun countdown_pastDate_returnsZero() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val past = today.minusDays(5).atStartOfDay(zone).toInstant().toEpochMilli()

        val days = CountdownCalculator.calculateDaysRemaining(past, zone, today)
        assertEquals(0L, days)
    }
}
