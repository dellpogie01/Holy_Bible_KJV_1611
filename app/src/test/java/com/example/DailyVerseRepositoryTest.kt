package com.example

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.BibleDatabase
import com.example.data.entity.VerseEntity
import com.example.data.repository.DailyVerseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyVerseRepositoryTest {
    private lateinit var database: BibleDatabase
    private lateinit var preferences: SharedPreferences
    private var now = date(2026, 10, 21, 12)
    private val verses = listOf(
        VerseEntity(id = 1, bookId = 1, chapter = 1, verseNum = 1, text = "Offline verse one"),
        VerseEntity(id = 2, bookId = 19, chapter = 23, verseNum = 1, text = "Offline verse two"),
        VerseEntity(id = 3, bookId = 43, chapter = 3, verseNum = 16, text = "Offline verse three")
    )

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        preferences = context.getSharedPreferences("daily_verse_test", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        database = Room.inMemoryDatabaseBuilder(context, BibleDatabase::class.java).build()
        database.bibleDao().insertVerses(verses)
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun repository() = DailyVerseRepository(database.bibleDao(), preferences) { now }

    @Test
    fun sameDaySelectionSurvivesRepositoryRecreationAndIsSavedLocally() = runBlocking {
        val selected = repository().getVerseForToday()!!
        assertTrue(selected in verses)
        assertEquals("2026-10-21", preferences.getString("selection_date", null))
        assertEquals(selected.bookId, preferences.getInt("book_id", -1))
        assertEquals(selected.chapter, preferences.getInt("chapter", -1))
        assertEquals(selected.verseNum, preferences.getInt("verse_num", -1))
        now = date(2026, 10, 21, 23, 59)
        repeat(10) { assertEquals(selected, repository().getVerseForToday()) }
    }

    @Test
    fun nextLocalDaySelectsDifferentVerseAndKeepsItForThatDay() = runBlocking {
        val previous = repository().getVerseForToday()
        now = date(2026, 10, 22, 0)
        val next = repository().getVerseForToday()
        assertNotNull(next)
        assertNotEquals(previous, next)
        assertEquals("2026-10-22", preferences.getString("selection_date", null))
        assertEquals(next, repository().getVerseForToday())
    }

    @Test
    fun rolloverNeverImmediatelyRepeatsAcrossManyDaysIncludingNewYear() = runBlocking {
        now = date(2026, 12, 31, 23, 59)
        var previous = repository().getVerseForToday()
        repeat(40) {
            now = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            val next = repository().getVerseForToday()
            assertTrue(next in verses)
            assertNotEquals(previous, next)
            previous = next
        }
    }

    @Test
    fun concurrentOpenAndResumeChecksKeepOneSelection() = runBlocking {
        val selector = repository()
        val selections = (1..20).map { async { selector.getVerseForToday() } }.awaitAll()
        assertNotNull(selections.first())
        assertEquals(1, selections.distinct().size)
    }

    @Test
    fun dateUsesLocalTimezoneRatherThanUtc() = runBlocking {
        now = date(2026, 10, 22, 0, 1) // Still October 21 in UTC.
        assertNotNull(repository().getVerseForToday())
        assertEquals("2026-10-22", preferences.getString("selection_date", null))
    }

    @Test
    fun emptyDatabaseDoesNotSaveDateAndCanRetryAfterOfflineContentLoads() = runBlocking {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        assertNull(repository().getVerseForToday())
        assertFalse(preferences.contains("selection_date"))
        database.bibleDao().insertVerses(verses)
        assertTrue(repository().getVerseForToday() in verses)
    }

    private fun date(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Calendar =
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei")).apply {
            clear()
            set(year, month - 1, day, hour, minute)
        }
}
