<div align="center">

# 🤖 Droid-Lite

### ⚡ An ultra-lightweight F-Droid-compatible client with universal repository support

**Everything essential, nothing unnecessary.**

[![License: GPLv3](https://img.shields.io/badge/License-GPLv3-F05032?style=for-the-badge&logo=gnu&logoColor=white)](./COPYING)
[![Based on Neo Store](https://img.shields.io/badge/Based_on-Neo_Store-00AA44?style=for-the-badge&logo=android&logoColor=white)](https://github.com/NeoApplications/Neo-Store)
[![APK 3.36 MiB](https://img.shields.io/badge/APK-3.36_MiB-3DDC84?style=for-the-badge&logo=android&logoColor=white)](TESTING.md)

</div>

---

## 📦 About

Droid-Lite is an **unofficial, ultra-lightweight F-Droid-compatible Android client** based on [Neo Store](https://github.com/NeoApplications/Neo-Store), with **universal repository support**.

- 🪶 **Tiny** — aggressively optimised APK (target: under 5 MB, stretch under 4 MB)
- 🌐 **Universal repos** — any compatible F-Droid repository, built-in catalogue + custom URLs
- 🔐 **Secure** — repository signature verification, APK checksum + signature checks, intact trust boundaries
- 🚀 **Fast** — no unnecessary background services, refresh only when you ask
- 🕵️ **Private** — no analytics, no tracking, no ads, no accounts

---

## 🧱 Based on Neo Store

Droid-Lite is a fork of **Neo Store** (by [Machiav3lli](https://github.com/Machiav3lli) and [NeoApplications](https://github.com/NeoApplications)) with a completely redesigned UI, a reduced dependency footprint, and aggressive size optimisation — while preserving the repository engine, installation pipeline and security model.

> **License:** GPLv3 — preserved from upstream. All applicable attribution to Neo Store and its contributors applies.

---

## 🚀 Features

- 📲 Browse, search, install, update and uninstall apps from any F-Droid-compatible repository
- 🌐 Built-in repository catalogue + add your own repos by URL (with optional fingerprint)
- 🔄 Per-repository refresh, enable/disable, signature verification and clear attribution
- 📝 App descriptions, version lists, changelogs where available
- 🛡️ APK integrity: SHA-256 verification + signature match against the repository certificate
- 🌗 Light, dark and black themes (plus dynamic colour on Android 12+)

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

## 📊 Size

| | Compressed APK |
|---|---|
| Neo Store (baseline) | 5.58 MiB |
| **Droid-Lite** | **3.36 MiB** |
| Reduction | **−39.8 %** |

See [TESTING.md](./TESTING.md) and [AUDIT.md](./AUDIT.md) for the full breakdown.

---

## 📄 License

GPLv3 — see [COPYING](./COPYING). This project preserves upstream Neo Store licensing and attribution.

---

<div align="center">

**Real repos. Real security. Minimal footprint.**

Made by AnshLabs716 · Android + Linux only

</div>
