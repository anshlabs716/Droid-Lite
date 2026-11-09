<div align="center">

# 🤖 Droid-Lite

### ⚡ An ultra-lightweight F-Droid client. Everything essential, nothing unnecessary.

[![License: GPLv3](https://img.shields.io/badge/License-GPLv3-F05032?style=for-the-badge&logo=gnu&logoColor=white)](./COPYING)
[![Based on Neo Store](https://img.shields.io/badge/Based_on-Neo_Store-00AA44?style=for-the-badge&logo=android&logoColor=white)](https://github.com/NeoApplications/Neo-Store)
[![APK 3.36 MiB](https://img.shields.io/badge/APK-3.36_MiB-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#-size)

</div>

---

## 📦 About

Droid-Lite is an **unofficial, ultra-lightweight F-Droid-compatible Android client** based on [Neo Store](https://github.com/NeoApplications/Neo-Store), with **universal repository support** — any F-Droid-format repo works, including ones you add yourself. No provider lock-in, no update needed to discover new repos.

Built to be **small and quiet**: tiny APK, no background sync unless you turn it on, no stats fetching, no trackers, no analytics, no accounts, no ads.

---

## 🚀 Features

- 📲 Browse, search, install, update and uninstall apps from any F-Droid-compatible repository
- 🌐 Built-in repository catalogue + add your own repos by URL (fingerprint optional but verified when given)
- 🔄 Per-repo refresh, enable/disable, signature verification, clear repo attribution on every app
- 📝 App descriptions, version lists, changelogs where the repo provides them
- 🛡️ Downloads are verified: SHA-256 integrity, package metadata, and APK signature must match the repo's signing certificate
- 🌗 Light / dark / black themes, dynamic colour on Android 12+

## 🔋 Lightweight by design

| | |
|---|---|
| **APK** | **3.36 MiB** — 39.8 % smaller than Neo Store's 5.58 MiB (see [TESTING.md](./TESTING.md)) |
| **Background** | Auto-sync **off by default** — refresh happens when you tap it, or on your schedule |
| **Network** | No stats/beacon requests, no telemetry; only downloads and index refreshes you trigger |
| **RAM/CPU** | 20 dependencies removed, custom ~120-line navigator instead of 3 navigation libs, no heavyweight UI framework |
| **Privacy** | No analytics, no tracking, no ads, no accounts — data stays on your device |

Full dependency audit in [AUDIT.md](./AUDIT.md), engineering plan in [PLAN.md](./PLAN.md).

---

## 🛠️ Build

Requirements: **JDK 21**, **Android SDK (compileSdk 37)**, Git

```bash
git clone https://github.com/anshlabs716/droid-lite.git
cd droid-lite
echo "sdk.dir=/path/to/android-sdk" > local.properties
./gradlew assembleRelease
```

APK: `build/outputs/apk/release/`

---

## 🙏 Credits

Forked from [Neo Store](https://github.com/NeoApplications/Neo-Store) by [Machiav3lli](https://github.com/Machiav3lli) and contributors — the repository engine, installer pipeline and security model are built on their work.

## 📄 License

GPLv3 — see [COPYING](./COPYING). Upstream licensing and attribution preserved.

---

<div align="center">

**Real repos. Real security. Minimal footprint.**

Made by AnshLabs716

</div>
