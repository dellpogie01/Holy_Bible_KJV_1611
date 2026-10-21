package com.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.ui.screen.SplashScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SplashScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun artworkAppearsImmediatelyAndSplashCompletesAfterThreeSeconds() {
        val completions = AtomicInteger()
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme { SplashScreen(onTimeout = { completions.incrementAndGet() }) }
        }
        compose.onNodeWithContentDescription("Holy Bible").assertExists()
        compose.mainClock.advanceTimeBy(2_999, ignoreFrameDuration = true)
        compose.runOnIdle { assertEquals(0, completions.get()) }
        compose.mainClock.advanceTimeBy(1, ignoreFrameDuration = true)
        compose.runOnIdle { assertEquals(1, completions.get()) }
        compose.mainClock.advanceTimeBy(3_000)
        compose.runOnIdle { assertEquals(1, completions.get()) }
    }
}
