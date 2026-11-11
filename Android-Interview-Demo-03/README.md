# Android Interview Demo 03 (UFM Sports)

An enterprise-grade native Android demonstration app built in Kotlin, demonstrating modern Android architecture, dependency injection, reactive state management, and paged data loading for a fantasy sports and tournament platform (**UFM Sports**).

---

## 📱 Project Overview

**Android Interview Demo 03** demonstrates advanced Android engineering practices tailored for high-performance, real-time sports and contest feeds. The app features a rich dashboard displaying featured tournaments, upcoming matches with team lineups, countdown timers, current offers, and user contest summaries.

### Key Highlights
- **Architecture**: Model-View-ViewModel (MVVM) with Repository Pattern and Clean Architecture separation.
- **Dependency Injection**: [Koin](https://insert-koin.io/) (`koin-android`, `koin-core`) for lightweight, testable dependency injection.
- **Asynchronous & Reactive**: Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`), `StateFlow`, `SharedFlow`, and `LiveData`.
- **Networking**: [Retrofit 2](https://square.github.io/retrofit/) + [OkHttp 3](https://square.github.io/okhttp/) with custom `Resource` sealed classes (`Success`, `Error`, `Loading`).
- **Pagination & Infinite Scrolling**: Custom `ApiDataLoaderPaged` and `addOnEndScrollListener` powering seamless pagination on both horizontal and vertical lists.
- **UI & Layouts**:
  - `ViewBinding` across all Activities, Fragments, and Adapters.
  - `ConcatAdapter` chaining dynamic content (`TournamentsRvAdapter`, `UpcomingMatchesRvAdapter`) with skeleton loading views (`LoadingViewAdapter`).
  - `DiffUtil` optimization (`MatchDiffUtilCallback`) for smooth list updates.
  - Interactive carousels using `CardSlider`.
  - Custom UI widgets: `GradientStrokeView`, `HorizontalSpacingItemDecoration`, and custom material dialogs (`dialog_no_internet`).
  - Scalable sizing using `sdp` (scalable DP) and `ssp` (scalable SP).
  - Modern typography powered by the `Nunito` font family.
- **Date & Time**: `ThreeTenABP` for reliable match scheduling, countdowns, and date-time operations.

---

## 📥 Download

- Pre-built release build is not published on GitHub yet. You can compile the debug build locally using `./gradlew assembleDebug` (or explore available builds in the root [Releases Catalog](../releases/README.md)).

---

## 🏗️ Codebase Architecture

```text
com.android.interviewdemo/
├── di/                     # Koin Dependency Injection modules (appModule)
├── model/
│   ├── local/              # Local data sources & ContentResolver access
│   ├── models/             # Domain entities (Match, TournamentList, Teams, UserData, etc.)
│   ├── remote/             # Retrofit service interface & client setup
│   └── repository/         # MainRepository abstracting network & local data
├── networking/             # NetworkResource wrappers & response parsers
├── ui/
│   ├── activities/         # MainActivity & host activities
│   ├── adapter/            # RecyclerView adapters, DiffUtil callbacks, ItemDecorations
│   ├── common/             # BaseActivity, BaseFragment, GlobalAction, BaseViewHolder
│   ├── customview/         # Custom UI views (GradientStrokeView)
│   ├── dialog/             # Material dialogs (e.g., No Internet connection dialog)
│   └── fragments/          # HomeFragment, EmptyFragment
├── utils/                  # ApiDataLoader, Resource<T>, SingleEvent, Extensions
└── viewmodel/              # MainActivityViewModel exposing LiveData & Flow streams
```

---

## 🛠️ Tech Stack & Dependencies

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin 1.9.0 |
| **Target / Compile SDK** | Android 14 (API 34), Min SDK: 24 |
| **Architecture** | MVVM, Repository Pattern, Clean Separation |
| **Dependency Injection** | Koin 3.5.6 (`koin-android`, `koin-core`) |
| **Async / Concurrency** | Kotlin Coroutines, StateFlow, SharedFlow, LiveData |
| **Networking & Serialization** | Retrofit 2.9.0, OkHttp 4.3.1, Gson Converter |
| **Image Loading** | Picasso 2.71828 |
| **Navigation** | Jetpack Navigation Component 2.3.5 |
| **UI Components** | ViewBinding, ConcatAdapter, DiffUtil, Material Design 1.12.0, CardSlider |
| **Screen Adaptation** | SDP (Scalable DP) 1.1.1, SSP (Scalable SP) 1.1.1 |
| **Time Management** | ThreeTenABP 1.4.0 |

---

## 🔨 How to Build & Run

### In Android Studio
1. Open Android Studio.
2. Select **Open** and choose the `Android-Interview-Demo-03` directory.
3. Allow Gradle to sync.
4. Select a device or emulator and click **Run** (`Shift + F10`).

### From Command Line
```bash
# Navigate to project directory
cd Android-Interview-Demo-03

# Assemble debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```
