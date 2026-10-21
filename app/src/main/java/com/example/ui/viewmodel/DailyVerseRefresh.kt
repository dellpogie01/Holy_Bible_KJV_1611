package com.example.ui.viewmodel

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Calendar

/** Called within the Home screen's RESUMED lifecycle, so backgrounding cancels the timer. */
internal suspend fun refreshDailyVerseWhileActive(
    check: () -> Unit,
    clock: () -> Calendar = { Calendar.getInstance() }
) {
    while (currentCoroutineContext().isActive) {
        check()
        val now = clock()
        val midnight = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Recalculate after clock/timezone edits as well as ordinary midnight (including DST).
        delay((midnight.timeInMillis - now.timeInMillis).coerceIn(1L, 60_000L))
    }
}
