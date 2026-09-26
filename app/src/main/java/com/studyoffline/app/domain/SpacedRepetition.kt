package com.studyoffline.app.domain

data class Sm2ReviewResult(
    val easeFactor: Float,
    val intervalDays: Int,
    val repetitions: Int,
    val dueAt: Long
)

object SpacedRepetition {
    /**
     * SM-2 Simplified implementation
     * Grade: 0 = Again, 1 = Hard, 2 = Good, 3 = Easy
     */
    fun calculateNextReview(
        currentEase: Float,
        currentInterval: Int,
        currentRepetitions: Int,
        grade: Int,
        now: Long = System.currentTimeMillis()
    ): Sm2ReviewResult {
        val safeGrade = grade.coerceIn(0, 3)
        val repetitions: Int
        val intervalDays: Int

        if (safeGrade == 0) {
            repetitions = 0
            intervalDays = 1
        } else {
            repetitions = currentRepetitions + 1
            intervalDays = when (repetitions) {
                1 -> 1
                2 -> 6
                else -> Math.round(currentInterval * currentEase).toInt().coerceAtLeast(1)
            }
        }

        // easeFactor = max(1.3, easeFactor + (0.1 - (3-g)*(0.08 + (3-g)*0.02)))
        val factorDiff = 0.1f - (3 - safeGrade) * (0.08f + (3 - safeGrade) * 0.02f)
        val newEaseFactor = maxOf(1.3f, currentEase + factorDiff)
        val dueAt = now + intervalDays * 86_400_000L

        return Sm2ReviewResult(
            easeFactor = newEaseFactor,
            intervalDays = intervalDays,
            repetitions = repetitions,
            dueAt = dueAt
        )
    }
}
