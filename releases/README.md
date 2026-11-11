# Build Releases Catalog

This folder contains pre-built APK releases downloaded from the respective GitHub repositories of the Android Native Examples collection.

---

## 📦 Summary of Available APK Releases

| Project / Application | File Name | Version | Build Type | Size | SHA-256 Checksum |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **Android Interview Demo 01** | [`android-interview-demo-01-debug.apk`](./android-interview-demo-01-debug.apk) | `v1.0` | Debug | ~6.17 MB | `24D38EC608EE094E7C579D437D92BBE8E7102E8ED4E3352481C611C2452D3FAC` |
| **Android Interview Demo 02 (Audio Recorder)** | [`android-interview-demo-02-debug.apk`](./android-interview-demo-02-debug.apk) | `v1.0` | Debug | ~5.58 MB | `6446246B8279DF72B0D8FA0007C766F7F8FE7937066666131C27A93F866767B9` |
| **Music Player** | [`music-player-v1.0.apk`](./music-player-v1.0.apk) | `v1.0` | Release | ~11.52 MB | `6F32BC3F34EA75301927B736E7A62E7ABDFB9D9455ACA56F24AB458070932F91` |
| **DevTools** | [`devtools-debug.apk`](./devtools-debug.apk) | `v1.0` | Debug | ~8.11 MB | `3C3F82D0F7C654415471E0D020CE57EC2F02026825CCDF2692993C5F6BBA730B` |

---

## 📱 Detailed Release Breakdown

### 1. Android Interview Demo 01
* **File:** `android-interview-demo-01-debug.apk`
* **Version:** 1.0 (versionCode: 1)
* **Application ID / Package:** `com.android.interviewdemo`
* **Description:** Demonstration app implementing MVVM architecture in Kotlin. Fetches list items from a remote API, renders item cards, and navigates to a detail view displaying full details and imagery.
* **Source Folder:** [`../Android-Interview-Demo-01`](../Android-Interview-Demo-01)
* **Release Tag:** `v1.0` (Initial release)

### 2. Android Interview Demo 02 (Audio Recorder)
* **File:** `android-interview-demo-02-debug.apk`
* **Version:** 1.0 (versionCode: 1)
* **Application ID / Package:** `com.android.interviewdemo`
* **Description:** Native Android audio recording application built with Kotlin and MVVM. Enables users to capture audio snippets, review them with playback controls, and manage saved recordings.
* **Source Folder:** [`../Android-Interview-Demo-02`](../Android-Interview-Demo-02)
* **Release Tag:** `v1.0` (Initial release)

### 3. Music Player
* **File:** `music-player-v1.0.apk`
* **Version:** 1.0 (versionCode: 1)
* **Application ID / Package:** `com.anubhav.musicplayer`
* **Description:** Native music playback app built with Kotlin following the MVVM architecture. Supports song list browsing, playback controls (play, pause, shuffle, stop), song metadata inspection, and background playback.
* **Source Folder:** [`../MusicPlayer`](../MusicPlayer)
* **Release Tag:** `v1.0` (Initial release)

### 4. DevTools
* **File:** `devtools-debug.apk`
* **Version:** 1.0 (versionCode: 1)
* **Application ID / Package:** `com.anubhav.devtools`
* **Description:** Developer utilities app providing an interactive Kotlin Playground with mobile-optimized WebView, DOM element snapshot capture, contact inspector, and foreground background service.
* **Source Folder:** [`../DevTools`](../DevTools)

---

## ℹ️ Repositories Without GitHub Releases

The following projects currently do not have pre-built APK releases published on GitHub. However, their clean source codes are included in this repository and can be compiled locally:

1. **Android-Interview-Demo-03 (`UFM Sports`)**:
   - Source code: [`../Android-Interview-Demo-03`](../Android-Interview-Demo-03)
   - To build an APK locally:
     ```bash
     cd Android-Interview-Demo-03
     ./gradlew assembleDebug
     ```
     The generated APK will be at `Android-Interview-Demo-03/app/build/outputs/apk/debug/app-debug.apk`.

2. **AndroidExample (`Android Interview Demo - Search & Detail`)**:
   - Source code: [`../AndroidExample`](../AndroidExample)
   - To build an APK locally:
     ```bash
     cd AndroidExample
     ./gradlew assembleDebug
     ```
     The generated APK will be at `AndroidExample/app/build/outputs/apk/debug/app-debug.apk`.

3. **MVVM-Retrofit-Kotlin-Example**:
   - Source code: [`../MVVM-Retrofit-Kotlin-Example`](../MVVM-Retrofit-Kotlin-Example)
   - To build an APK locally:
     ```bash
     cd MVVM-Retrofit-Kotlin-Example
     ./gradlew assembleDebug
     ```
     The generated APK will be at `MVVM-Retrofit-Kotlin-Example/app/build/outputs/apk/debug/app-debug.apk`.

