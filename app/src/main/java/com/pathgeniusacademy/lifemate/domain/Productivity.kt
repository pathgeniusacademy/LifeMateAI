package com.pathgeniusacademy.lifemate.domain

import java.time.LocalDate

object Productivity {
    fun streak(dates: List<String>, today: LocalDate, legacyDate: String? = null, legacyStreak: Int = 0): Int {
        val known = dates.toSet()
        var cursor = if (today.toString() in known) today else today.minusDays(1)
        var count = 0
        while (cursor.toString() in known) {
            count++
            if (cursor.toString() == legacyDate) return count + (legacyStreak - 1).coerceAtLeast(0)
            cursor = cursor.minusDays(1)
        }
        return count
    }
    fun secondsLeft(deadline: Long, now: Long): Int =
        ((deadline - now).coerceAtLeast(0L).plus(999) / 1000).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}
