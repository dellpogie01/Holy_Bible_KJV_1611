package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.BibleDatabase
import com.example.data.entity.BookEntity
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.ReadingPositionEntity
import com.example.data.entity.VerseEntity
import com.example.data.repository.BibleRepository
import com.example.data.repository.DailyVerseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BibleRepository
    private val dailyVerseRepository: DailyVerseRepository

    var books = mutableStateOf<List<BookEntity>>(emptyList())
        private set

    var currentBook = mutableStateOf<BookEntity?>(null)
        private set

    var currentChapter = mutableStateOf(1)
        private set

    var currentChapterVerses = mutableStateOf<List<VerseEntity>>(emptyList())
        private set

    var searchResults = mutableStateOf<List<VerseEntity>>(emptyList())
        private set

    var isSearching = mutableStateOf(false)
        private set

    var searchErrorMessage = mutableStateOf<String?>(null)
        private set

    var fontSizeSp = mutableStateOf(18f)
        private set

    var themeMode = mutableStateOf(com.example.ui.theme.AppThemeMode.SYSTEM)
        private set

    val bookmarks: StateFlow<List<BookmarkEntity>>

    val readingPosition: StateFlow<ReadingPositionEntity?>

    var verseOfTheDay = mutableStateOf<VerseEntity?>(null)
        private set

    init {
        val dao = BibleDatabase.getDatabase(application).bibleDao()
        repository = BibleRepository(dao)
        dailyVerseRepository = DailyVerseRepository(
            dao,
            application.getSharedPreferences("daily_verse_kjv", android.content.Context.MODE_PRIVATE)
        )
        viewModelScope.launch {
            dao.observeVerseCount().collect { count ->
                if (count > 0) checkAndUpdateVerseOfTheDay()
            }
        }

        bookmarks = repository.getAllBookmarks().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        readingPosition = repository.getReadingPositionFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        val prefs = application.getSharedPreferences("bible_preferences", android.content.Context.MODE_PRIVATE)
        val savedTheme = prefs.getString("theme_mode", com.example.ui.theme.AppThemeMode.SYSTEM.name) ?: com.example.ui.theme.AppThemeMode.SYSTEM.name
        themeMode.value = try {
            com.example.ui.theme.AppThemeMode.valueOf(savedTheme)
        } catch (e: Exception) {
            com.example.ui.theme.AppThemeMode.SYSTEM
        }

        loadBooks()
        checkAndUpdateVerseOfTheDay()
    }

    private fun loadBooks() {
        viewModelScope.launch {
            repository.observeAllBooks().collect { allBooks ->
                books.value = allBooks
                if (currentBook.value == null && allBooks.isNotEmpty()) {
                    currentBook.value = allBooks.getOrNull(42) ?: allBooks.first()
                }
            }
        }
    }

    fun checkAndUpdateVerseOfTheDay() {
        viewModelScope.launch {
            dailyVerseRepository.getVerseForToday()?.let { verseOfTheDay.value = it }
        }
    }

    fun selectBookAndChapter(book: BookEntity, chapter: Int) {
        currentBook.value = book
        currentChapter.value = chapter
        viewModelScope.launch {
            currentChapterVerses.value = repository.getVersesForChapter(book.id, chapter)
            repository.saveReadingPosition(book.id, book.name, chapter, 1)
        }
    }

    fun nextChapter() {
        val book = currentBook.value ?: return
        if (currentChapter.value < book.totalChapters) {
            selectBookAndChapter(book, currentChapter.value + 1)
        } else {
            // Move to next book if available
            val currentIdx = books.value.indexOfFirst { it.id == book.id }
            if (currentIdx != -1 && currentIdx < books.value.size - 1) {
                val nextBook = books.value[currentIdx + 1]
                selectBookAndChapter(nextBook, 1)
            }
        }
    }

    fun previousChapter() {
        val book = currentBook.value ?: return
        if (currentChapter.value > 1) {
            selectBookAndChapter(book, currentChapter.value - 1)
        } else {
            // Move to previous book if available
            val currentIdx = books.value.indexOfFirst { it.id == book.id }
            if (currentIdx > 0) {
                val prevBook = books.value[currentIdx - 1]
                selectBookAndChapter(prevBook, prevBook.totalChapters)
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            searchResults.value = emptyList()
            searchErrorMessage.value = null
            return
        }

        viewModelScope.launch {
            isSearching.value = true
            searchErrorMessage.value = null

            // Check if query is a Bible reference like "John 3:16" or "Psalm 23"
            val refMatch = parseReference(query)
            if (refMatch != null) {
                val (bookId, chapter) = refMatch
                val verses = repository.getVersesForChapter(bookId, chapter)
                searchResults.value = verses
                if (verses.isEmpty()) {
                    searchErrorMessage.value = "No verses found for reference: $query"
                }
            } else {
                // FTS keyword search
                val cleanQuery = query.trim().replace(Regex("[^a-zA-Z0-9 ]"), "")
                if (cleanQuery.isNotBlank()) {
                    val results = repository.searchVerses("$cleanQuery*")
                    searchResults.value = results
                    if (results.isEmpty()) {
                        searchErrorMessage.value = "No matching verses found for \"$query\""
                    }
                } else {
                    searchResults.value = emptyList()
                }
            }
            isSearching.value = false
        }
    }

    private fun parseReference(query: String): Pair<Int, Int>? {
        // e.g. "John 3" or "Psalm 23"
        val parts = query.trim().split(Regex("\\s+"))
        if (parts.size < 2) return null
        val chapterStr = parts.last()
        val chapter = chapterStr.toIntOrNull() ?: return null
        val bookNamePart = parts.dropLast(1).joinToString(" ").lowercase()

        val matchedBook = books.value.find { it.name.lowercase().contains(bookNamePart) }
        return matchedBook?.let { Pair(it.id, chapter) }
    }

    fun toggleBookmark(verse: VerseEntity, bookName: String) {
        viewModelScope.launch {
            repository.toggleBookmark(verse.bookId, bookName, verse.chapter, verse.verseNum, verse.text)
        }
    }

    suspend fun isBookmarked(bookId: Int, chapter: Int, verseNum: Int): Boolean {
        return repository.isBookmarked(bookId, chapter, verseNum)
    }

    fun removeBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(id)
        }
    }

    fun increaseFontSize() {
        if (fontSizeSp.value < 32f) fontSizeSp.value += 2f
    }

    fun decreaseFontSize() {
        if (fontSizeSp.value > 12f) fontSizeSp.value -= 2f
    }

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        themeMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("bible_preferences", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("theme_mode", mode.name).apply()
    }
}
