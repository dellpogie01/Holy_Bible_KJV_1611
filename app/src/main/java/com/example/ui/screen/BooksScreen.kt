package com.example.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.entity.BookEntity
import com.example.ui.theme.GoldPrimary

@Composable
fun BooksScreen(
    books: List<BookEntity>,
    initialTab: String = "OT", // "OT" or "NT"
    onSelectBook: (BookEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf(initialTab) }

    val filteredBooks = books.filter { it.testament == selectedTab }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Bible Books",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        // Testament Tabs
        TabRow(
            selectedTabIndex = if (selectedTab == "OT") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldPrimary
        ) {
            Tab(
                selected = selectedTab == "OT",
                onClick = { selectedTab = "OT" },
                text = { Text("Old Testament (39)") },
                selectedContentColor = GoldPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("tab_old_testament")
            )
            Tab(
                selected = selectedTab == "NT",
                onClick = { selectedTab = "NT" },
                text = { Text("New Testament (27)") },
                selectedContentColor = GoldPrimary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("tab_new_testament")
            )
        }

        // Books List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredBooks) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBook(book) }
                        .testTag("book_item_${book.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = book.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${book.totalChapters} Chapters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select Book",
                            tint = GoldPrimary
                        )
                    }
                }
            }
        }
    }
}
