# SONORA 🎵
### Letterboxd for Music – Android Social Network

> **A social platform for music lovers to discover, rate, review, log listening diary, create curated lists, and receive AI-powered recommendations.**  
> Built as a 5-Unit Software Engineering Graduation Project (תכנות טלפונים חכמים תשפ"ו - 883589).

---

## 🌟 Key Features

- 🎵 **Discover & Search:** Search through millions of songs, albums, and artists via the live **iTunes REST API** or explore the curated local classic catalog.
- ⭐ **Rate & Review:** Give star ratings (0.5 to 5.0) and write detailed reviews with community rating distribution histograms.
- ❤️ **Social Feed:** Follow friends and music critics, like reviews with animated feedback, and leave comments.
- 📖 **Music Diary (Log):** Maintain a chronological diary of all songs and albums listened to, with dates, ratings, notes, and **Re-listen** tags.
- 📋 **Curated Lists:** Create custom public and personal playlists/ranked lists (e.g., *"Top 10 Albums of All Time"*).
- 🤖 **Sonora AI Recommender:** Intelligent recommendation engine that analyzes your listening diary and 4+ star ratings, calculates preference affinity vectors, and provides personalized suggestions with textual AI explanations and a semantic **Mood Prompt Generator**.
- 🔔 **Background Service & Notifications:** Android `Service` running background synchronization and delivering `NotificationChannel` updates when new AI recommendations or social interactions occur.
- 🎧 **Audio Preview:** In-app 30-second audio preview player powered by `MediaPlayer`.
- 🖼️ **Profile & Avatar:** Customize bio, view favorite 4 pinned albums, and pick avatar pictures using Android's `ActivityResultLauncher`.

---

## 🏛️ Architecture & Tech Stack

- **Platform:** Native Android (minSdk 34, compileSdk 36)
- **Language:** Java (Java 11)
- **UI:** Material Design 3 (Dark Theme inspired by Letterboxd), ConstraintLayout, Fragments & BottomNavigationView
- **Database:** Relational SQLite Database with 9 tables, foreign keys, transactions, and advanced queries (`JOIN`, `GROUP BY`)
- **Networking:** Asynchronous `HttpURLConnection` with multi-threaded `ExecutorService` and main thread `Handler`
- **Caching:** In-memory `LruCache` for album artworks
- **Async Execution:** `ExecutorService` thread pool (zero UI thread freezing)

---

## 📂 Project Structure

```
com.yonatan.sonora
├── activities/       # SplashActivity, AuthActivity, MusicDetailActivity, UserProfileActivity, etc.
├── adapters/         # RecyclerView Adapters (MusicItemAdapter, ReviewAdapter, DiaryAdapter, etc.)
├── ai/               # SonoraAIEngine (Content-based filtering & Mood generator)
├── api/              # ItunesApiClient (External REST API integration)
├── database/         # SonoraDatabaseHelper, SonoraRepository (Relational DB & CRUD)
├── fragments/        # FeedFragment, DiscoverFragment, AIRecommendationsFragment, DiaryFragment, ProfileFragment
├── models/           # OOP hierarchy (MusicItem, Song, Album, Artist, User, Review, DiaryEntry, MusicList)
├── services/         # SonoraSyncService (Background sync & notifications), SonoraBroadcastReceiver
└── utils/            # ImageLoader (LRU Cache), AudioPreviewPlayer, SessionManager, FormatUtils
```

---

## 🚀 Getting Started

1. Clone this repository:
   ```bash
   git clone https://github.com/yonatangolbandi-crypto/Sonora.git
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run on an Android Device or Emulator.
4. On the login screen, click **"כניסה מהירה עם משתמש הדגמה (יונתן)"** for one-click testing with pre-loaded diary entries, reviews, and recommendations!

---

## 📄 License
This project was created for educational purposes as part of the Israeli Bagrut 5 Units Software Engineering project.
