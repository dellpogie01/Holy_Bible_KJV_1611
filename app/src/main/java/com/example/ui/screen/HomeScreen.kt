package com.example.ui.screen

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.animatedRainbowBorder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.viewmodel.refreshDailyVerseWhileActive
import com.example.data.entity.BookEntity
import com.example.data.entity.ReadingPositionEntity
import com.example.data.entity.VerseEntity
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavySurface

@Composable
fun HomeScreen(
    books: List<BookEntity> = emptyList(),
    readingPosition: ReadingPositionEntity?,
    verseOfTheDay: VerseEntity?,
    onCheckVerseOfTheDay: () -> Unit = {},
    onNavigateToBooks: (String) -> Unit, // "OT" or "NT"
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onOpenChapter: (bookId: Int, chapter: Int) -> Unit,
    onToggleBookmark: (VerseEntity, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Recheck on entry/resume and at local midnight. The minute cap also catches clock/timezone edits.
    val lifecycleOwner = LocalLifecycleOwner.current
    val checkDailyVerse = rememberUpdatedState(onCheckVerseOfTheDay)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            refreshDailyVerseWhileActive(check = { checkDailyVerse.value() })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.home_title_artwork),
                    contentDescription = "Holy Bible",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Column {
                    Text(
                        text = "Holy Bible",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "King James Version 1611",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .size(44.dp)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = GoldPrimary
                    )
                }
            }
        }

        // Bible Verse of the Day Card (Directly above CONTINUE READING section)
        val dailyBook = books.find { it.id == verseOfTheDay?.bookId }
        if (verseOfTheDay != null && dailyBook != null) {
            val bookName = dailyBook.name
            val reference = "$bookName ${verseOfTheDay.chapter}:${verseOfTheDay.verseNum}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animatedRainbowBorder()
                    .testTag("verse_of_the_day_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VERSE OF THE DAY",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "\"${verseOfTheDay.text}\" — $reference (King James Version)"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share verse")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("share_verse_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share verse",
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "\"${verseOfTheDay.text}\"",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = reference,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Continue Reading Card
        if (readingPosition != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenChapter(readingPosition.bookId, readingPosition.chapter)
                    }
                    .testTag("continue_reading_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONTINUE READING",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${readingPosition.bookName} ${readingPosition.chapter}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Chapter",
                        tint = GoldPrimary
                    )
                }
            }
        }

        // Testament Browse Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToBooks("OT") }
                    .testTag("old_testament_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Old Testament",
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Old Testament",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "39 Books",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToBooks("NT") }
                    .testTag("new_testament_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "New Testament",
                        tint = GoldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "New Testament",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "27 Books",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
