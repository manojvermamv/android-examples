# Android Native Examples

A consolidated collection of native Android sample applications and interview demonstrations built with Kotlin, modern Android Architecture Components (MVVM, ViewModel, LiveData, Coroutines), Jetpack libraries, and Retrofit.

All source code has been cleanly imported as independent subdirectories with their original files, resources, and markdown documentation preserved (without git submodules).

---

## 📂 Repository Index

| # | Project Directory | App / Focus | Architecture & Key Tech | Original Documentation | APK Build Release |
| :-: | :--- | :--- | :--- | :--- | :---: |
| 1 | [`Android-Interview-Demo-01`](./Android-Interview-Demo-01) | **Item List & Detail Demo** | Kotlin, MVVM, LiveData, ViewModel, Dummy API Integration | [README.md](./Android-Interview-Demo-01/README.md) | [Download v1.0 APK](./releases/android-interview-demo-01-debug.apk) |
| 2 | [`Android-Interview-Demo-02`](./Android-Interview-Demo-02) | **Audio Recorder & Player** | Kotlin, MVVM, MediaRecorder, Audio Playback Controls | [README.md](./Android-Interview-Demo-02/README.md) | [Download v1.0 APK](./releases/android-interview-demo-02-debug.apk) |
| 3 | [`AndroidExample`](./AndroidExample) | **Android Interview Demo 03** | Kotlin, MVVM, Item Search & Detail Views, Clean Separation | [README.md](./AndroidExample/README.md) | Source build |
| 4 | [`MusicPlayer`](./MusicPlayer) | **Native Music Player App** | Kotlin, MVVM, MediaPlayer, Background Audio Service | [README.md](./MusicPlayer/README.md) | [Download v1.0 APK](./releases/music-player-v1.0.apk) |
| 5 | [`MVVM-Retrofit-Kotlin-Example`](./MVVM-Retrofit-Kotlin-Example) | **MVVM ft. Retrofit & LiveData** | Kotlin, MVVM, Retrofit REST API, LiveData, Repository Pattern | [README.md](./MVVM-Retrofit-Kotlin-Example/README.md) | Source build |

---

## 🚀 Pre-Built APK Releases

All available release APK binaries have been downloaded directly from their respective GitHub releases and placed into the central [`releases/`](./releases) directory.

For full release details, checksums, and package names, see [`releases/README.md`](./releases/README.md).

### Quick Download Links
- 📥 **Android Interview Demo 01**: [`releases/android-interview-demo-01-debug.apk`](./releases/android-interview-demo-01-debug.apk) (v1.0, ~6.17 MB)
- 📥 **Android Interview Demo 02**: [`releases/android-interview-demo-02-debug.apk`](./releases/android-interview-demo-02-debug.apk) (v1.0, ~5.58 MB)
- 📥 **Music Player**: [`releases/music-player-v1.0.apk`](./releases/music-player-v1.0.apk) (v1.0, ~11.52 MB)

---

## 🛠️ Project Overviews

### 1. [Android-Interview-Demo-01](./Android-Interview-Demo-01)
- **Description:** Demonstrates standard Android interview requirements assessing coding quality, architecture, and maintainability. Implements a clean MVVM structure fetching data from mock/public REST APIs, displaying an item list, and navigating to a detailed item view.
- **Source Directory:** [`Android-Interview-Demo-01`](./Android-Interview-Demo-01)

### 2. [Android-Interview-Demo-02](./Android-Interview-Demo-02)
- **Description:** A dedicated audio recording application providing intuitive audio capture, recording review, audio playback controls, and recording file management (rename, delete, play).
- **Source Directory:** [`Android-Interview-Demo-02`](./Android-Interview-Demo-02)

### 3. [AndroidExample](./AndroidExample)
- **Description:** An interview demo implementation focusing on item listing, filtering, search, and detail transitions with clean MVVM architecture in Kotlin.
- **Source Directory:** [`AndroidExample`](./AndroidExample)

### 4. [MusicPlayer](./MusicPlayer)
- **Description:** A native Android audio player with support for audio playlists, playback controls (play, pause, shuffle, stop), song metadata rendering, and background audio service.
- **Source Directory:** [`MusicPlayer`](./MusicPlayer)

### 5. [MVVM-Retrofit-Kotlin-Example](./MVVM-Retrofit-Kotlin-Example)
- **Description:** A comprehensive architectural tutorial project demonstrating step-by-step implementation of MVVM with Retrofit 2, Kotlin Coroutines, LiveData, ViewModel, and Repository Pattern in Android.
- **Source Directory:** [`MVVM-Retrofit-Kotlin-Example`](./MVVM-Retrofit-Kotlin-Example)

---

## 🔨 How to Build from Source

Each sub-project is a standalone Android project. You can open any sub-project directly in Android Studio or compile it from the command line:

```bash
# Example: Building Android-Interview-Demo-01
cd Android-Interview-Demo-01
./gradlew assembleDebug

# Example: Building MusicPlayer
cd ../MusicPlayer
./gradlew assembleRelease
```

