# SpyCodeReverseEng (Android Malware Analysis & Reverse Engineering Study)

A native Android security research and reverse-engineering case study analyzing the architecture, threat vectors, command & control (C2) mechanisms, and payload behavior of an Android Remote Access Trojan (RAT) / Spyware application (internally known as *SNK RAT* / *CodeReverse*).

> [!CAUTION]
> **Educational & Security Research Notice**: This project codebase is provided strictly for educational purposes, mobile security research, reverse-engineering study, and threat intelligence analysis. Do not deploy or execute malicious payloads outside of controlled sandbox environments.

---

## 🔬 Research & Threat Overview

**SpyCodeReverseEng** represents a reverse-engineered and decompiled analysis of a multi-featured Android Remote Access Trojan. The project deconstructs how stealth Android malware utilizes system broadcast receivers, foreground services, accessibility services, and remote socket connections to monitor and exfiltrate device telemetry.

### Key Capabilities Analyzed in the Codebase
- **SMS Interception (`smscatcher`)**: Background BroadcastReceiver capturing incoming and outgoing SMS traffic (`RECEIVE_SMS`, `READ_SMS`, `BROADCAST_SMS`) and logging device identifiers alongside message payloads.
- **C2 Socket Communication (`socket` / `server`)**: Real-time bi-directional WebSocket and raw socket communication channels for receiving commands and streaming exfiltrated data back to remote servers without port-forwarding requirements.
- **Targeted Application Enumeration**: Manifest `<queries>` configuration auditing installed targets (e.g., WhatsApp, WhatsApp Business, Instagram, Facebook, Messenger, Truecaller).
- **Device Telemetry & Exfiltration**: Extraction of device identifiers (model, manufacturer, release version, device ID), network status (Wi-Fi, mobile carrier, phone state), and coarse/fine GPS coordinates.
- **Persistence & Auto-Start**: Foreground services with wake locks, device admin hooks, and broadcast listeners designed to maintain execution across device reboots and app kills.
- **Push Triggering via Firebase**: Integration with Firebase Cloud Messaging (FCM) to trigger on-demand wakeups and remote task execution.
- **Social Engineering UI Vectors (`uiapp`)**: Decoy user interfaces including phishing screens, fake notification prompts, and permission escalation flows designed to convince targets to grant high-privilege system access.

---

## 📥 Download Release

- Download analyzed build APK: [spycode-reverse-eng-release-v1.apk](../releases/spycode-reverse-eng-release-v1.apk) (see [Releases Catalog](../releases/README.md))

> [!NOTE]
> This APK binary is preserved exclusively for security audits, static analysis (with tools like JADX, Ghidra, or APKTool), and dynamic sandbox detonation (such as MobSF).

---

## 🏗️ Codebase Architecture

```text
com.anubhav.codereverse/
├── database/               # Local SQLite / persistence layer caching exfiltrated logs
├── server/                 # C2 server connection management & communication protocols
├── services/               # Persistent foreground services, wake locks & background task engines
├── uiapp/
│   ├── adapters/           # UI list adapters for decoy screens
│   ├── common/             # Base activities, components, and helper wrappers
│   ├── fcm/                # Firebase Cloud Messaging listener service & push handlers
│   ├── firebase/           # Realtime database / cloud integration helpers
│   └── ui/                 # Decoy activities (login, register, home, settings, permissions)
├── utils/
│   ├── smscatcher/         # SMS broadcast interception and payload serialization
│   └── socket/             # Socket.io client implementation for live command execution
└── com.anubhav.commonutility/
    ├── backgroundtasks/    # Async worker threads and task executors
    ├── customfont/         # Typography handlers for custom typography injection
    └── views/              # Custom widget implementations
```

---

## 🛠️ Analyzed Tech Stack & Components

| Category | Technology |
| :--- | :--- |
| **Language** | Kotlin & Java |
| **Package ID** | `com.anubhav.codereverse` |
| **Compile SDK / Min SDK** | Android SDK (Target: API 30+, Min: API 21+) |
| **Messaging & C2** | Socket.IO client, WebSockets, Firebase Cloud Messaging (FCM) |
| **Location Services** | Google Play Services Location & Places API |
| **Local Persistence** | SQLite / Room Database |
| **Networking & HTTP** | OkHttp 3, Retrofit 2 |
| **MultiDex** | Enabled (`multiDexEnabled true`) |

---

## 🔨 Static & Dynamic Analysis Guide

To inspect or decompile the codebase using standard security auditing tools:

```bash
# Decompile the included APK using JADX
jadx-gui releases/spycode-reverse-eng-release-v1.apk

# Disassemble resources and Smali bytecode via APKTool
apktool d releases/spycode-reverse-eng-release-v1.apk -o decoded_apk
```
