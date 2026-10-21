package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.BottomNav
import com.example.ui.components.Screen
import com.example.ui.screen.*
import com.example.ui.theme.HolyBibleTheme
import com.example.ui.viewmodel.BibleViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BibleViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.checkAndUpdateVerseOfTheDay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { it.remove() }
        }
        enableEdgeToEdge()
        setContent {
            val themeMode = viewModel.themeMode.value
            HolyBibleTheme(themeMode = themeMode) {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: BibleViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val readingPosition by viewModel.readingPosition.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val verseOfTheDay = viewModel.verseOfTheDay.value
    val books = viewModel.books.value

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Books.route,
        Screen.Search.route,
        Screen.Bookmarks.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                BottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    books = books,
                    readingPosition = readingPosition,
                    verseOfTheDay = verseOfTheDay,
                    onCheckVerseOfTheDay = { viewModel.checkAndUpdateVerseOfTheDay() },
                    onNavigateToBooks = { testament ->
                        navController.navigate("books_tab/$testament")
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    },
                    onOpenChapter = { bookId, chapter ->
                        val book = books.find { it.id == bookId } ?: books.firstOrNull()
                        if (book != null) {
                            viewModel.selectBookAndChapter(book, chapter)
                            navController.navigate("reader")
                        }
                    },
                    onToggleBookmark = { verse, bookName ->
                        viewModel.toggleBookmark(verse, bookName)
                    }
                )
            }

            composable(Screen.Books.route) {
                BooksScreen(
                    books = books,
                    initialTab = "OT",
                    onSelectBook = { book ->
                        viewModel.selectBookAndChapter(book, 1)
                        navController.navigate("chapter_select/${book.id}")
                    }
                )
            }

            composable(
                route = "books_tab/{testament}",
                arguments = listOf(navArgument("testament") { type = NavType.StringType })
            ) { backStackEntry ->
                val testament = backStackEntry.arguments?.getString("testament") ?: "OT"
                BooksScreen(
                    books = books,
                    initialTab = testament,
                    onSelectBook = { book ->
                        viewModel.selectBookAndChapter(book, 1)
                        navController.navigate("chapter_select/${book.id}")
                    }
                )
            }

            composable(
                route = "chapter_select/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.IntType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getInt("bookId") ?: 1
                val book = books.find { it.id == bookId } ?: books.firstOrNull()
                    ?: return@composable
                ChapterSelectScreen(
                    book = book,
                    onSelectChapter = { chapterNum ->
                        viewModel.selectBookAndChapter(book, chapterNum)
                        navController.navigate("reader")
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("reader") {
                val currentBook = viewModel.currentBook.value
                val currentChapter = viewModel.currentChapter.value
                val verses = viewModel.currentChapterVerses.value

                ChapterReaderScreen(
                    book = currentBook,
                    chapter = currentChapter,
                    verses = verses,
                    fontSizeSp = viewModel.fontSizeSp.value,
                    onIncreaseFontSize = { viewModel.increaseFontSize() },
                    onDecreaseFontSize = { viewModel.decreaseFontSize() },
                    onNextChapter = { viewModel.nextChapter() },
                    onPreviousChapter = { viewModel.previousChapter() },
                    onToggleBookmark = { verse, bookName ->
                        viewModel.toggleBookmark(verse, bookName)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    books = books,
                    searchResults = viewModel.searchResults.value,
                    isSearching = viewModel.isSearching.value,
                    errorMessage = viewModel.searchErrorMessage.value,
                    onSearch = { query -> viewModel.search(query) },
                    onSelectVerseChapter = { bookId, chapter ->
                        val book = books.find { it.id == bookId }
                        if (book != null) {
                            viewModel.selectBookAndChapter(book, chapter)
                            navController.navigate("reader")
                        }
                    }
                )
            }

            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    bookmarks = bookmarks,
                    onSelectBookmark = { bookId, chapter ->
                        val book = books.find { it.id == bookId }
                        if (book != null) {
                            viewModel.selectBookAndChapter(book, chapter)
                            navController.navigate("reader")
                        }
                    },
                    onDeleteBookmark = { id ->
                        viewModel.removeBookmark(id)
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    currentTheme = viewModel.themeMode.value,
                    onThemeSelected = { mode ->
                        viewModel.setThemeMode(mode)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
