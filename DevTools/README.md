# DevTools

A native Android developer utility application built in Kotlin, implementing the MVVM architecture with Koin Dependency Injection, background services, and an interactive Kotlin Playground integration.

---

## 📱 Project Overview

**DevTools** provides convenient on-device utilities for developers, combining native device interactions with an interactive, web-powered Kotlin development environment.

### Key Features
- **Interactive Kotlin Playground**:
  - Embedded Android `WebView` running official [Kotlin Playground](https://play.kotlinlang.org/).
  - Custom JavaScript viewport injection for responsive code editing and execution on mobile.
  - One-tap toggle for **Desktop Mode** user-agent rendering.
  - In-app DOM element screenshot capture (`captureElementByClass`) to snapshot and share Kotlin code snippets directly.
- **Device Tools & Contacts**:
  - Contact inspection using Android's `ContentResolver`.
  - Modern runtime permissions handling with fallback dialogs to System Settings.
- **Background Foreground Service**:
  - Dedicated `MainService` foreground service managing lifecycle, service binding, and persistent notifications.
- **Modern Architecture**:
  - Clean Model-View-ViewModel (MVVM) separation.
  - Dependency Injection with [Koin](https://insert-koin.io/) (`koin-android`, `koin-core`).
  - ViewBinding across activities and views.
  - Event-driven state handling via `SingleEvent` and lifecycle-aware observers.

---

## 📥 Download

- Download debug build APK: [devtools-debug.apk](../releases/devtools-debug.apk) (see [Releases Catalog](../releases/README.md))

---

## 🏗️ Codebase Architecture

```text
com.anubhav.devtools/
├── di/                     # Koin Dependency Injection module (appModule)
├── model/
│   ├── local/              # Local data sources (ContentDataSource / ContentResolver)
│   ├── models/             # Data models (Tool, ToolNames, Contact, User)
│   ├── repository/         # NavRepository handling data operations
│   └── retrofit/           # Retrofit client and API interface definitions
├── service/                # MainService foreground service with local binder
├── ui/
│   ├── activities/         # MainActivity (tool grid & WebView playground)
│   ├── adapter/            # ToolRvAdapter (grid view of tools)
│   └── common/             # BaseActivity, BaseFragment
├── utils/                  # FileUtils, ViewUtils, WebViewUtils, Permissions, SingleEvent
└── viewmodel/              # MainActivityViewModel managing tools and state
```

---

## 🛠️ Tech Stack & Dependencies

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin 1.9.0 |
| **Target / Compile SDK** | Android 14 (API 34), Min SDK: 24 |
| **Architecture** | MVVM, Clean Separation |
| **Dependency Injection** | Koin 3.5.6 (`koin-android`, `koin-core`) |
| **Background Processing** | Foreground Service (`MainService`), Coroutines |
| **Web Integration** | Android WebView with custom JS injection & DOM capture |
| **Networking** | Retrofit 2.9.0, OkHttp 4.3.1, Gson Converter |
| **Image Loading** | Picasso 2.71828 |
| **Navigation** | Jetpack Navigation Component 2.3.5 |
| **UI Components** | ViewBinding, Material Components 1.12.0, GridLayoutManager |

---

## 🔨 How to Build & Run

### In Android Studio
1. Open Android Studio.
2. Select **Open** and choose the `DevTools` directory.
3. Allow Gradle to sync dependencies.
4. Run the app on a connected device or emulator (`Shift + F10`).

### From Command Line
```bash
# Navigate to project directory
cd DevTools

# Assemble debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```
