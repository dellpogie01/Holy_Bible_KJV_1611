package com.example.data.repository

import com.example.data.dao.BibleDao
import com.example.data.entity.BookEntity
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.ReadingPositionEntity
import com.example.data.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

class BibleRepository(private val bibleDao: BibleDao) {

    fun observeAllBooks(): Flow<List<BookEntity>> = bibleDao.observeAllBooks()

    suspend fun getAllBooks(): List<BookEntity> = bibleDao.getAllBooks()

    suspend fun getBooksByTestament(testament: String): List<BookEntity> = bibleDao.getBooksByTestament(testament)

    suspend fun getBookById(bookId: Int): BookEntity? = bibleDao.getBookById(bookId)

    suspend fun getVersesForChapter(bookId: Int, chapter: Int): List<VerseEntity> = bibleDao.getVersesForChapter(bookId, chapter)

    fun getVersesForChapterFlow(bookId: Int, chapter: Int): Flow<List<VerseEntity>> = bibleDao.getVersesForChapterFlow(bookId, chapter)

    suspend fun searchVerses(query: String): List<VerseEntity> = bibleDao.searchVerses(query)

    suspend fun getVerse(bookId: Int, chapter: Int, verseNum: Int): VerseEntity? = bibleDao.getVerse(bookId, chapter, verseNum)

    fun getAllBookmarks(): Flow<List<BookmarkEntity>> = bibleDao.getAllBookmarks()

    suspend fun isBookmarked(bookId: Int, chapter: Int, verseNum: Int): Boolean {
        return bibleDao.getBookmark(bookId, chapter, verseNum) != null
    }

    suspend fun toggleBookmark(bookId: Int, bookName: String, chapter: Int, verseNum: Int, text: String) {
        val existing = bibleDao.getBookmark(bookId, chapter, verseNum)
        if (existing != null) {
            bibleDao.deleteBookmarkByVerse(bookId, chapter, verseNum)
        } else {
            bibleDao.insertBookmark(
                BookmarkEntity(
                    bookId = bookId,
                    bookName = bookName,
                    chapter = chapter,
                    verseNum = verseNum,
                    text = text
                )
            )
        }
    }

    suspend fun deleteBookmark(id: Long) {
        bibleDao.deleteBookmarkById(id)
    }

    fun getReadingPositionFlow(): Flow<ReadingPositionEntity?> = bibleDao.getReadingPositionFlow()

    suspend fun saveReadingPosition(bookId: Int, bookName: String, chapter: Int, verseNum: Int) {
        bibleDao.saveReadingPosition(
            ReadingPositionEntity(
                id = 1,
                bookId = bookId,
                bookName = bookName,
                chapter = chapter,
                verseNum = verseNum
            )
        )
    }
}
