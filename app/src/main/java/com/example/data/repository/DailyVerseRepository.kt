package com.example.data.repository

import android.content.SharedPreferences
import com.example.data.dao.BibleDao
import com.example.data.entity.VerseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Calendar

/** The preferences belong to the existing offline KJV database, not to reading position. */
class DailyVerseRepository(
    private val dao: BibleDao,
    private val preferences: SharedPreferences,
    private val clock: () -> Calendar = { Calendar.getInstance() }
) {
    private val mutex = Mutex()

    suspend fun getVerseForToday(): VerseEntity? = withContext(Dispatchers.IO) {
        mutex.withLock {
            while (true) {
                val today = localDate(clock())
                val bookId = preferences.getInt("book_id", -1)
                val chapter = preferences.getInt("chapter", -1)
                val verseNum = preferences.getInt("verse_num", -1)
                val saved = dao.getVerse(bookId, chapter, verseNum)
                if (preferences.getString("selection_date", null) == today && saved != null) {
                    if (localDate(clock()) != today) continue
                    return@withLock saved
                }

                // Exclude the entire reference, rather than a row ID that could change on reimport.
                val selected = dao.getRandomVerse(bookId, chapter, verseNum)
                    ?: return@withLock null // Content may still be seeding; its Flow triggers a retry.
                if (localDate(clock()) != today) continue // Midnight occurred during the query.

                // Write the reference and date together before publishing the verse to the UI.
                if (preferences.edit()
                        .putInt("book_id", selected.bookId)
                        .putInt("chapter", selected.chapter)
                        .putInt("verse_num", selected.verseNum)
                        .putString("selection_date", today)
                        .commit()) {
                    return@withLock selected
                }
                return@withLock null
            }
            @Suppress("UNREACHABLE_CODE")
            null
        }
    }

    private fun localDate(calendar: Calendar): String =
        "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
}
