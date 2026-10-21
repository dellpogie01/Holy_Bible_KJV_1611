package com.example.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BookEntity
import com.example.data.entity.VerseEntity
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NavySurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReaderScreen(
    book: BookEntity?,
    chapter: Int,
    verses: List<VerseEntity>,
    fontSizeSp: Float,
    onIncreaseFontSize: () -> Unit,
    onDecreaseFontSize: () -> Unit,
    onNextChapter: () -> Unit,
    onPreviousChapter: () -> Unit,
    onToggleBookmark: (VerseEntity, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedVerseForAction by remember { mutableStateOf<VerseEntity?>(null) }
    var isBookmarked by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "${book?.name ?: ""} $chapter",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_button")) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = onDecreaseFontSize, modifier = Modifier.testTag("decrease_font_size")) {
                    Text("A-", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onIncreaseFontSize, modifier = Modifier.testTag("increase_font_size")) {
                    Text("A+", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Verses List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(verses) { verse ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedVerseForAction = verse
                            showBottomSheet = true
                        }
                        .padding(vertical = 4.dp)
                        .testTag("verse_item_${verse.verseNum}"),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${verse.verseNum}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp * 0.75f).sp
                        ),
                        color = GoldPrimary,
                        modifier = Modifier.width(32.dp)
                    )
                    Text(
                        text = verse.text,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.5f).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom Chapter Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPreviousChapter,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.testTag("previous_chapter_button")
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous", color = GoldPrimary)
                }

                Text(
                    text = "Chapter $chapter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onNextChapter,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.testTag("next_chapter_button")
                ) {
                    Text("Next", color = GoldPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = GoldPrimary)
                }
            }
        }
    }

    // Verse Action Bottom Sheet
    if (showBottomSheet && selectedVerseForAction != null) {
        val verse = selectedVerseForAction!!
        val bookName = book?.name ?: ""

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "$bookName $chapter:${verse.verseNum}",
                    style = MaterialTheme.typography.titleLarge,
                    color = GoldPrimary
                )
                Text(
                    text = "\"${verse.text}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Copy
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Bible Verse", "\"${verse.text}\" — $bookName $chapter:${verse.verseNum} (KJV)")
                            clipboard.setPrimaryClip(clip)
                            showBottomSheet = false
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = GoldPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Copy", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Share
                    TextButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "\"${verse.text}\" — $bookName $chapter:${verse.verseNum} (KJV)")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                            showBottomSheet = false
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = GoldPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Share", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Bookmark
                    TextButton(
                        onClick = {
                            onToggleBookmark(verse, bookName)
                            showBottomSheet = false
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = GoldPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bookmark", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
