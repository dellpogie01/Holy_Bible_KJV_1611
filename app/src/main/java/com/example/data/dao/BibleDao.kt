package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BookEntity
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.ReadingPositionEntity
import com.example.data.entity.VerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {
    @Query("SELECT * FROM books ORDER BY bookOrder ASC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM books ORDER BY bookOrder ASC")
    fun observeAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(*) FROM verses")
    fun observeVerseCount(): Flow<Int>

    @Query("""
        SELECT * FROM verses
        WHERE NOT (bookId = :previousBookId AND chapter = :previousChapter AND verseNum = :previousVerseNum)
        ORDER BY RANDOM() LIMIT 1
    """)
    suspend fun getRandomVerse(previousBookId: Int, previousChapter: Int, previousVerseNum: Int): VerseEntity?

    @Query("SELECT * FROM books WHERE testament = :testament ORDER BY bookOrder ASC")
    suspend fun getBooksByTestament(testament: String): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBookById(bookId: Int): BookEntity?

    @Query("SELECT * FROM verses WHERE bookId = :bookId AND chapter = :chapter ORDER BY verseNum ASC")
    suspend fun getVersesForChapter(bookId: Int, chapter: Int): List<VerseEntity>

    @Query("SELECT * FROM verses WHERE bookId = :bookId AND chapter = :chapter ORDER BY verseNum ASC")
    fun getVersesForChapterFlow(bookId: Int, chapter: Int): Flow<List<VerseEntity>>

    @Query("""
        SELECT verses.* FROM verses 
        JOIN verses_fts ON verses.rowid = verses_fts.rowid 
        WHERE verses_fts MATCH :query
        LIMIT 100
    """)
    suspend fun searchVerses(query: String): List<VerseEntity>

    @Query("SELECT * FROM verses WHERE bookId = :bookId AND chapter = :chapter AND verseNum = :verseNum LIMIT 1")
    suspend fun getVerse(bookId: Int, chapter: Int, verseNum: Int): VerseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND chapter = :chapter AND verseNum = :verseNum LIMIT 1")
    suspend fun getBookmark(bookId: Int, chapter: Int, verseNum: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND chapter = :chapter AND verseNum = :verseNum")
    suspend fun deleteBookmarkByVerse(bookId: Int, chapter: Int, verseNum: Int)

    // Reading Position
    @Query("SELECT * FROM reading_position WHERE id = 1")
    fun getReadingPositionFlow(): Flow<ReadingPositionEntity?>

    @Query("SELECT * FROM reading_position WHERE id = 1")
    suspend fun getReadingPosition(): ReadingPositionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingPosition(position: ReadingPositionEntity)
}
