# Holy Bible - KJV 1611 Android App Implementation Plan

A feature-rich, high-performance offline King James Version (1611) Bible reading and search application built with Kotlin, Jetpack Compose, Material 3, and Room SQLite database.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Dark Mode**: User selected **True black OLED dark mode** for a peaceful nighttime reading experience.
> - **Search**: User selected **Fast SQLite FTS offline search across all 66 books**.
> - **Verse of the Day**: User selected **Daily Verse card on home screen with share and bookmark options**.

- **Confirmed Decision 1**: Complete offline KJV dataset bundled in Room database (all 39 Old Testament books, 27 New Testament books, chapters, and verses).
- **Confirmed Decision 2**: Navigation via Jetpack Navigation Compose with a sleek bottom bar (Home, Books, Search, Bookmarks).

---

## 1. Overview & Core Concept

- **What It Does**: Provides a distraction-free, lightning-fast Bible reading experience with full offline search, bookmarking, verse copying/sharing, daily verse inspiration, and reading position persistence.
- **Target Audience / Persona**: Christians and Bible scholars seeking a reverent, clean, offline-first study and reading app optimized for Android devices.
- **Key Value**: 100% offline capability, zero registration required, instant full-text search, and gorgeous OLED dark-mode aesthetics.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Splash Screen**: Launch with glowing golden cross and peaceful deep navy branding, fading smoothly into Home.
  2. **Home Screen**: Features "Continue Reading" card, Verse of the Day card, quick jump to Old / New Testament, and search shortcut.
  3. **Bible Browsing**: Select Testament -> Book -> Chapter -> Verse reader.
  4. **Verse Action Sheet**: Tap any verse to Copy, Share, or Bookmark.
  5. **Search Screen**: Type keywords or references (e.g., "John 3:16", "love", "faith") to instantly find matching verses with highlighted terms.
  6. **Bookmarks Screen**: Manage saved verses with quick navigation back to the source chapter.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Peaceful, sacred, and minimalist Christian aesthetic.
  - *Color Palette & Mood*: True black background (`#0B0D12`), Deep Royal Navy (`#111827`), Rich Gold accents (`#D4AF37` / `#E5C158`), and Crisp Warm White typography (`#F9FAFB`).
  - *Typography & Hierarchy*: Material 3 typography with clean serif/sans-serif pairing for maximum legibility during long reading sessions.
  - *Component Styling*: Rounded cards with subtle gold outlines and elevation, generous touch targets (min 48dp).

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1**: Bundled Room SQLite Database with Pre-populated KJV Verses.
  - *Chosen Approach*: Include a robust SQLite database asset or programmatic initial seeding of KJV verses on first launch.
  - *Why*: Guarantees instant 100% offline access without network requests or large asset file download bottlenecks.
- **Decision 2**: FTS4 (Full Text Search) in SQLite.
  - *Chosen Approach*: Use SQLite FTS4 virtual tables for lightning-fast keyword searches across thousands of verses.
  - *Why*: Sub-millisecond response times even on budget Android devices.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                        UI Layer                        │
│   (Jetpack Compose + Material 3 + Navigation Compose)  │
└───────────────────────────┬────────────────────────────┘
                            │ StateFlow / Actions
┌───────────────────────────▼────────────────────────────┐
│                    ViewModel Layer                     │
│    (BibleViewModel, SearchViewModel, BookmarkVM)       │
└───────────────────────────┬────────────────────────────┘
                            │ Repository Pattern
┌───────────────────────────▼────────────────────────────┐
│                    Data & Storage Layer                │
│       (Room Database + FTS4 Virtual Table + DAO)       │
└────────────────────────────────────────────────────────┘
```

- **Data Model & Entities**:
  - `BookEntity`: id, name, testament ("OT" or "NT"), totalChapters, bookOrder
  - `VerseEntity`: id, bookId, chapter, verseNum, text
  - `BookmarkEntity`: id, bookId, bookName, chapter, verseNum, text, timestamp
  - `ReadingPositionEntity`: id, bookId, bookName, chapter, verseNum, timestamp
- **Key Modules to Implement**:
  1. `Database & Seeding`: `BibleDatabase.kt`, `BibleDao.kt`, `BookEntity.kt`, `VerseEntity.kt`, `BookmarkEntity.kt`, `ReadingPositionEntity.kt`. Preload all 66 books and essential verses (Genesis, Psalms, Gospels, Revelation, etc., plus full complete Bible generation utility or robust seed dataset of KJV texts).
  2. `Repositories`: `BibleRepository.kt` managing local queries, FTS search, bookmarks, and reading progress.
  3. `ViewModels`: `BibleViewModel.kt` handling UI state, current book/chapter navigation, search query flow, and bookmarks.
  4. `UI Composables`:
     - `SplashScreen.kt`: Reverent deep navy & gold cross launch animation.
     - `HomeScreen.kt`: Continue reading, Verse of the Day, OT/New Testament cards, quick search.
     - `BooksScreen.kt`: Tabbed or grouped Old/New Testament book picker.
     - `ChapterScreen.kt`: Chapter reader with adjustable font size, verse tapping for copy/share/bookmark.
     - `SearchScreen.kt`: Offline instant keyword & reference search with highlighted terms.
     - `BookmarksScreen.kt`: List of saved verses.
     - `BottomNav.kt`: Clean navigation bar.
