# Droid-Lite — Security & Compatibility Test Report

Build: v1.0.0-droidlite (versionCode 1) · release APK 3,525,658 bytes · upstream Neo Store 1.3.3 @ 37f8ee3

## 1. Security verification (static, per source)

| Check | Location | Status |
|---|---|---|
| Index JAR signature validation | `RepositoryUpdater.validateCertificate` (line ~867) | ✅ Intact — unchanged from upstream |
| Certificate fingerprint calculation + user-fingerprint compare | `RepositoryUpdater.calculateFingerprint`, update() flow | ✅ Intact — unchanged |
| Fingerprint mismatch between index + jar treated as failure | RepositoryUpdater (fingerprintFromIndex != fingerprintFromJar → error) | ✅ Intact |
| APK SHA-256 (or index hashType) integrity check | `DownloadWorker.validatePackage` (line ~462) | ✅ Intact |
| APK packageName / versionCode metadata check | `DownloadWorker.validatePackage` | ✅ Intact |
| APK signer certificate must match repository signature | `DownloadWorker.validatePackage` → `signerSHA256Signatures` | ✅ Intact |
| Permission-delta gate for auto-updates | `validatePackage` permission checks | ✅ Intact |
| `DisableSignatureCheck` default | Preferences: `Value.BooleanValue(false)` | ✅ Default OFF |
| `DisableCertificateValidation` default | Preferences: `Value.BooleanValue(false)` | ✅ Default OFF (expert opt-in only) |
| Custom repo trust model | New repos require explicit fingerprint entry; failure surfaced via sync errors | ✅ Unchanged (generic, any URL) |
| TLS: OkHttp default validation | `Downloader.createDownloadClient`, `CoilDownloader.createClient` | ✅ Certificate trust-all only behind expert pref |
| Download size overrun guard | `DownloadSizeException` on declared-length overrun | ✅ Ported to OkHttp rewrite |
| Resume (Range) + If-Modified-Since / If-None-Match | Downloader | ✅ Ported |

## 2. Compatibility checks (build + unit level)

- **Index v1 + v2** parsing preserved (v2 with entry.jar/diff updates). v1 parser now kotlinx-serialization (was Jackson) — same `IndexV1` model, `ignoreUnknownKeys = true`.
- **minSdk 26 / targetSdk 36** unchanged; `LegacyInstaller` (pre-26) removed as dead path for minSdk 26.
- **DB migration**: version 1304 → 1305 via Room AutoMigration with explicit `@DeleteTable` spec (exodus_info, download_stats, ds_file_meta_data + 3 stats views). Schema exported (`1305.json`).
- **Universal repo support**: repository add/remove/enable/disable/refresh + signature verification all go through the unchanged generic `Repository` pipeline — no provider hardcoding. Intent filters for `fdroidrepo(s)://` links preserved in AndroidManifest.
- **Installers**: Session (default), System, Root (libsu), Shizuku, AppManager retained; Legacy removed.

## 3. Unit tests — PASS

`gradlew :testDebugUnitTest` → BUILD SUCCESSFUL

| Suite | Tests | Failures | Errors |
|---|---|---|---|
| IndexV2MergerTest (index v2 merge logic) | 10 | 0 | 0 |

## 4. Runtime testing — pending on-device pass

This machine has **no emulator or system images** (SDK: platforms/build-tools only, `adb devices` → none attached), so the following checklist requires a device/run and is NOT claimed complete:

- [ ] First launch + onboarding
- [ ] Repository setup against f-droid.org + IzzyOnDroid
- [ ] Signature-failure path (bad fingerprint repo)
- [ ] Custom repo add/remove/enable/disable + per-repo refresh
- [ ] Search, duplicate packages across repos (attribution shown)
- [ ] Download → verification → install → update → uninstall
- [ ] Offline behaviour, empty repo, invalid URL, very large index (e.g. full F-Droid main index ~40 MB)
- [ ] Android 8 / 12 / 14 / 15 device matrix

The release APK is unsigned (`assembleRelease` without signing config) — for installation testing, sign with a debug keystore or run `assembleDebug`.

## 5. Known size contributor

`assets/PublicSuffixDatabase.list` (132 KB, 42 KB compressed) — OkHttp's cookie-domain database. AGP 9.4 `packaging.resources.excludes` does not strip it (verified by rebuild). Harmless: the app never sends or stores cookies.
