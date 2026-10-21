package com.example

import com.example.ui.viewmodel.refreshDailyVerseWhileActive
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class DailyVerseRefreshTest {
    @Test
    fun openHomeChecksImmediatelyAndAgainAtLocalMidnight() = runTest {
        val start = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei")).apply {
            clear()
            set(2026, Calendar.OCTOBER, 21, 23, 59, 58)
        }.timeInMillis
        val dates = mutableListOf<Int>()
        fun now() = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei")).apply {
            timeInMillis = start + currentTime
        }
        val watcher = launch {
            refreshDailyVerseWhileActive(
                check = { dates.add(now().get(Calendar.DAY_OF_MONTH)) }, clock = ::now
            )
        }
        runCurrent()
        assertEquals(listOf(21), dates)
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(listOf(21, 22), dates)
        watcher.cancel()
        advanceTimeBy(120_000)
        assertEquals(2, dates.size)

        // The lifecycle restarts the watcher when Home resumes, causing an immediate check.
        val resumed = launch {
            refreshDailyVerseWhileActive(check = { dates.add(22) }, clock = ::now)
        }
        runCurrent()
        assertEquals(listOf(21, 22, 22), dates)
        resumed.cancel()
    }
}
