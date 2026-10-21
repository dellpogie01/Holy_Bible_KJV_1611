package com.example

import android.app.Application
import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.BookEntity
import com.example.data.entity.VerseEntity
import com.example.ui.screen.HomeScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyVerseShareTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun shareUsesCurrentlyDisplayedVerseAfterReplacementAndPreservesKjv() {
        val verse = mutableStateOf(VerseEntity(1, 1, 1, 1, "First displayed verse"))
        compose.setContent {
            MaterialTheme {
                HomeScreen(
                    books = listOf(BookEntity(1, "Genesis", "OT", 50, 1)),
                    readingPosition = null,
                    verseOfTheDay = verse.value,
                    onNavigateToBooks = {}, onNavigateToSearch = {}, onOpenChapter = { _, _ -> }
                )
            }
        }
        compose.runOnIdle {
            verse.value = VerseEntity(2, 1, 2, 3, "Replacement displayed verse")
        }
        compose.onNodeWithTag("share_verse_button").performClick()
        val chooser = shadowOf(ApplicationProvider.getApplicationContext<Application>()).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        @Suppress("DEPRECATION")
        val share = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, share.action)
        val text = share.getStringExtra(Intent.EXTRA_TEXT)!!
        assertTrue(text.contains("Replacement displayed verse"))
        assertTrue(text.contains("Genesis 2:3 (King James Version)"))
        assertTrue(!text.contains("First displayed verse"))
    }
}
