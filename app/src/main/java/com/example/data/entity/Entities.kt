package com.example.data.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val testament: String, // "OT" or "NT"
    val totalChapters: Int,
    val bookOrder: Int
)

@Entity(
    tableName = "verses",
    indices = [Index("bookId"), Index("chapter")]
)
data class VerseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val chapter: Int,
    val verseNum: Int,
    val text: String
)

@Fts4(contentEntity = VerseEntity::class)
@Entity(tableName = "verses_fts")
data class VerseFts(
    val text: String
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verseNum: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_position")
data class ReadingPositionEntity(
    @PrimaryKey val id: Int = 1,
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verseNum: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
