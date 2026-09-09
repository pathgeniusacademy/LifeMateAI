package com.pathgeniusacademy.lifemate.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ProductivityTest {
    private val today = LocalDate.of(2026, 9, 9)

    @Test fun undoAndRecheckPreserveYesterday() {
        val dates = listOf("2026-09-07", "2026-09-08", "2026-09-09")
        assertEquals(3, Productivity.streak(dates, today))
        assertEquals(2, Productivity.streak(dates.dropLast(1), today))
        assertEquals(3, Productivity.streak(dates.dropLast(1) + "2026-09-09", today))
    }
    @Test fun aMissedDayBreaksTheStreak() {
        assertEquals(0, Productivity.streak(listOf("2026-09-07"), today))
        assertEquals(1, Productivity.streak(listOf("2026-09-07", "2026-09-09"), today))
    }
    @Test fun migrationKeepsExistingStreakWithoutInventingDailyHistory() {
        assertEquals(12, Productivity.streak(listOf("2026-09-08"), today, "2026-09-08", 12))
        assertEquals(13, Productivity.streak(listOf("2026-09-08", "2026-09-09"), today, "2026-09-08", 12))
        assertEquals(0, Productivity.streak(listOf("2026-09-08"), today.plusDays(1), "2026-09-08", 12))
    }
    @Test fun duplicatesDoNotInflateStreaks() {
        assertEquals(1, Productivity.streak(listOf("2026-09-09", "2026-09-09"), today))
        assertEquals(0, Productivity.streak(emptyList(), today))
    }
    @Test fun timerRestoresFromDeadlineRatherThanScreenTicks() {
        assertEquals(1500, Productivity.secondsLeft(1_500_000L, 0))
        assertEquals(900, Productivity.secondsLeft(1_500_000L, 600_000L))
        assertEquals(1, Productivity.secondsLeft(1_500_000L, 1_499_999L))
        assertEquals(0, Productivity.secondsLeft(1_500_000L, 1_500_000L))
        assertEquals(0, Productivity.secondsLeft(1_500_000L, 1_800_000L))
    }
}
