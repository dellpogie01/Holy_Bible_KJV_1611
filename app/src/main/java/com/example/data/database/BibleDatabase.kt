package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BibleDao
import com.example.data.entity.BookEntity
import com.example.data.entity.BookmarkEntity
import com.example.data.entity.ReadingPositionEntity
import com.example.data.entity.VerseEntity
import com.example.data.entity.VerseFts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookEntity::class,
        VerseEntity::class,
        VerseFts::class,
        BookmarkEntity::class,
        ReadingPositionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        fun getDatabase(context: Context): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_kjv_database.db"
                )
                    .addCallback(BibleDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class BibleDatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(getDatabase(context).bibleDao())
                }
            }
        }

        suspend fun populateDatabase(dao: BibleDao) {
            val otBooks = listOf(
                BookEntity(1, "Genesis", "OT", 50, 1),
                BookEntity(2, "Exodus", "OT", 40, 2),
                BookEntity(3, "Leviticus", "OT", 27, 3),
                BookEntity(4, "Numbers", "OT", 36, 4),
                BookEntity(5, "Deuteronomy", "OT", 34, 5),
                BookEntity(6, "Joshua", "OT", 24, 6),
                BookEntity(7, "Judges", "OT", 21, 7),
                BookEntity(8, "Ruth", "OT", 4, 8),
                BookEntity(9, "1 Samuel", "OT", 31, 9),
                BookEntity(10, "2 Samuel", "OT", 24, 10),
                BookEntity(11, "1 Kings", "OT", 22, 11),
                BookEntity(12, "2 Kings", "OT", 25, 12),
                BookEntity(13, "1 Chronicles", "OT", 29, 13),
                BookEntity(14, "2 Chronicles", "OT", 36, 14),
                BookEntity(15, "Ezra", "OT", 10, 15),
                BookEntity(16, "Nehemiah", "OT", 13, 16),
                BookEntity(17, "Esther", "OT", 10, 17),
                BookEntity(18, "Job", "OT", 42, 18),
                BookEntity(19, "Psalms", "OT", 150, 19),
                BookEntity(20, "Proverbs", "OT", 31, 20),
                BookEntity(21, "Ecclesiastes", "OT", 12, 21),
                BookEntity(22, "Song of Solomon", "OT", 8, 22),
                BookEntity(23, "Isaiah", "OT", 66, 23),
                BookEntity(24, "Jeremiah", "OT", 52, 24),
                BookEntity(25, "Lamentations", "OT", 5, 25),
                BookEntity(26, "Ezekiel", "OT", 48, 26),
                BookEntity(27, "Daniel", "OT", 12, 27),
                BookEntity(28, "Hosea", "OT", 14, 28),
                BookEntity(29, "Joel", "OT", 3, 29),
                BookEntity(30, "Amos", "OT", 9, 30),
                BookEntity(31, "Obadiah", "OT", 1, 31),
                BookEntity(32, "Jonah", "OT", 4, 32),
                BookEntity(33, "Micah", "OT", 7, 33),
                BookEntity(34, "Nahum", "OT", 3, 34),
                BookEntity(35, "Habakkuk", "OT", 3, 35),
                BookEntity(36, "Zephaniah", "OT", 3, 36),
                BookEntity(37, "Haggai", "OT", 2, 37),
                BookEntity(38, "Zechariah", "OT", 14, 38),
                BookEntity(39, "Malachi", "OT", 4, 39)
            )

            val ntBooks = listOf(
                BookEntity(40, "Matthew", "NT", 28, 40),
                BookEntity(41, "Mark", "NT", 16, 41),
                BookEntity(42, "Luke", "NT", 24, 42),
                BookEntity(43, "John", "NT", 21, 43),
                BookEntity(44, "Acts", "NT", 28, 44),
                BookEntity(45, "Romans", "NT", 16, 45),
                BookEntity(46, "1 Corinthians", "NT", 16, 46),
                BookEntity(47, "2 Corinthians", "NT", 13, 47),
                BookEntity(48, "Galatians", "NT", 6, 48),
                BookEntity(49, "Ephesians", "NT", 6, 49),
                BookEntity(50, "Philippians", "NT", 4, 50),
                BookEntity(51, "Colossians", "NT", 4, 51),
                BookEntity(52, "1 Thessalonians", "NT", 5, 52),
                BookEntity(53, "2 Thessalonians", "NT", 3, 53),
                BookEntity(54, "1 Timothy", "NT", 6, 54),
                BookEntity(55, "2 Timothy", "NT", 4, 55),
                BookEntity(56, "Titus", "NT", 3, 56),
                BookEntity(57, "Philemon", "NT", 1, 57),
                BookEntity(58, "Hebrews", "NT", 13, 58),
                BookEntity(59, "James", "NT", 5, 59),
                BookEntity(60, "1 Peter", "NT", 5, 60),
                BookEntity(61, "2 Peter", "NT", 3, 61),
                BookEntity(62, "1 John", "NT", 5, 62),
                BookEntity(63, "2 John", "NT", 1, 63),
                BookEntity(64, "3 John", "NT", 1, 64),
                BookEntity(65, "Jude", "NT", 1, 65),
                BookEntity(66, "Revelation", "NT", 22, 66)
            )

            val books = otBooks + ntBooks
            dao.insertBooks(books)

            val verses = mutableListOf<VerseEntity>()

            val specificVerses = listOf(
                VerseEntity(bookId = 1, chapter = 1, verseNum = 1, text = "In the beginning God created the heaven and the earth."),
                VerseEntity(bookId = 1, chapter = 1, verseNum = 2, text = "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters."),
                VerseEntity(bookId = 1, chapter = 1, verseNum = 3, text = "And God said, Let there be light: and there was light."),
                VerseEntity(bookId = 19, chapter = 23, verseNum = 1, text = "The LORD is my shepherd; I shall not want."),
                VerseEntity(bookId = 19, chapter = 23, verseNum = 2, text = "He maketh me to lie down in green pastures: he leadeth me beside the still waters."),
                VerseEntity(bookId = 19, chapter = 23, verseNum = 3, text = "He restoreth my soul: he leadeth me in the paths of righteousness for his name's sake."),
                VerseEntity(bookId = 19, chapter = 23, verseNum = 4, text = "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me."),
                VerseEntity(bookId = 19, chapter = 23, verseNum = 5, text = "Thou preparest a table before me in the presence of mine enemies: thou anointest my head with oil; my cup runneth over."),
                VerseEntity(bookId = 19, chapter = 23, verseNum = 6, text = "Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the LORD for ever."),
                VerseEntity(bookId = 19, chapter = 119, verseNum = 105, text = "Thy word is a lamp unto my feet, and a light unto my path."),
                VerseEntity(bookId = 20, chapter = 3, verseNum = 5, text = "Trust in the LORD with all thine heart; and lean not unto thine own understanding."),
                VerseEntity(bookId = 20, chapter = 3, verseNum = 6, text = "In all thy ways acknowledge him, and he shall direct thy paths."),
                VerseEntity(bookId = 23, chapter = 40, verseNum = 31, text = "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint."),
                VerseEntity(bookId = 43, chapter = 3, verseNum = 16, text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."),
                VerseEntity(bookId = 43, chapter = 14, verseNum = 6, text = "Jesus saith unto him, I am the way, the truth, and the life: no man cometh unto the Father, but by me."),
                VerseEntity(bookId = 45, chapter = 8, verseNum = 28, text = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."),
                VerseEntity(bookId = 50, chapter = 4, verseNum = 6, text = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God."),
                VerseEntity(bookId = 50, chapter = 4, verseNum = 7, text = "And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus."),
                VerseEntity(bookId = 50, chapter = 4, verseNum = 13, text = "I can do all things through Christ which strengtheneth me.")
            )

            verses.addAll(specificVerses)

            for (book in books) {
                for (ch in 1..book.totalChapters) {
                    for (v in 1..10) {
                        if (verses.any { it.bookId == book.id && it.chapter == ch && it.verseNum == v }) {
                            continue
                        }
                        val sampleText = when {
                            book.id == 1 && ch == 1 -> "In the beginning God created the heaven and the earth. And the Spirit of God moved upon the face of the waters."
                            book.testament == "OT" -> "And the word of the LORD came unto ${book.name} in chapter $ch verse $v, saying, Walk ye in my statutes and keep my commandments."
                            else -> "Grace be unto you, and peace, from God our Father, and from the Lord Jesus Christ, as written in ${book.name} chapter $ch verse $v."
                        }
                        verses.add(
                            VerseEntity(
                                bookId = book.id,
                                chapter = ch,
                                verseNum = v,
                                text = sampleText
                            )
                        )
                    }
                }
            }

            dao.insertVerses(verses)

            dao.saveReadingPosition(
                ReadingPositionEntity(
                    id = 1,
                    bookId = 43,
                    bookName = "John",
                    chapter = 3,
                    verseNum = 16
                )
            )
        }
    }
}
