# Droid-Lite — Neo Store Repository Audit

Date: 2026-11-08 · Upstream: NeoApplications/Neo-Store @ `37f8ee3` (v1.3.3, versionCode 1304)

## 1. Architecture & build system

- **Single-module** Android app (`src/main`, plus `src/neo`/`src/debug` res-only overlay dirs — build-type overlays, not product flavors).
- Kotlin 2.4.21, AGP 9.4.1, Gradle 9.8.2, JDK toolchain **21**, compileSdk **37**, minSdk **26**, targetSdk **36**.
- UI: **Jetpack Compose** (BOM 2026.09.00, Material 3 beta line, adaptive, navigation3).
- DI: **Koin** (annotations + KSP). DB: **Room** (KSP). BG work: **WorkManager**.
- Networking: **Ktor client on OkHttp engine** (both pulled in) + raw OkHttp.
- Images: **Coil 3** (ktor3 + okhttp + compose artifacts all included).
- Release build: R8 minify + resource shrink ON, `proguard-android-optimize.txt` + custom `proguard.pro`, `isCrunchPngs=false`, dependenciesInfo stripped.
- ~350 Kotlin files, ~47.8k LOC. LOC split: **ui 27k (57%)**, data 10.4k, manager 5.2k, utils 2.3k, viewmodels 1.6k.

## 2. Dependency inventory (with purpose + verdict)

| Dependency | Purpose | Verdict for Droid-Lite |
|---|---|---|
| compose.* (runtime/ui/foundation/m3/adaptive/nav3) | Entire UI | Keep m3 core; **drop adaptive suite + navigation3 + navigation-compose** (replace with tiny state-based nav) |
| koin (5 artifacts + plugin) | DI | **Replace** with manual singletons (object graph) — app is small enough |
| room (3) | App/Repo/Release DB | Keep (core to catalogue) |
| work-runtime | Sync/download/install workers | Keep (essential; runs only on user action) |
| ktor (4) + okhttp (2) | Downloads, index fetch | **Drop Ktor entirely**; keep OkHttp only (Ktor wraps OkHttp anyway) |
| coil (4 artifacts) | Icons/screenshots | **Reduce** to coil-core + coil-compose (one networker) |
| zxing-core | QR repo sharing | **Remove** (non-essential) |
| vico (2) | Download-stats chart | **Remove** (non-essential) |
| material-kolor | Dynamic theming extras | **Remove** (use platform dynamicColor) |
| compose-html | HTML descriptions | **Remove** (parse to plain text/AnnotatedString minimal) |
| markdown (unused!) | Markdown rendering | **Already unused** — remove |
| jackson-core | JSON for index v1 | **Remove** — kotlinx.serialization already present; v1 parser can use it |
| libsu | Root installer | Keep (optional installer path) — small |
| shizuku (4 artifacts) | Shizuku installer | Keep (optional installer path) |
| hiddenapi-bypass | Shizuku/refine support | Keep with shizuku |
| biometric | Lock screen | **Remove** (non-essential) |
| simple-storage | SAF folder picker | **Remove**; use `ACTION_OPEN_DOCUMENT_TREE` directly |
| kotlinx-serialization-json | Prefs + index v2 | Keep |
| kotlinx-datetime / immutables / desugar | utils | datetime→java.time (drop), immutables keep (tiny), desugar keep |
| accompanist-permissions | Perm requests | **Remove**; use `rememberLauncherForActivityResult` |
| material (views MDC) | XML themes | **Remove** (Compose-only) |
| androidx.preference | Prefs screen | **Remove**; app stores prefs in SharedPreferences already via its own wrapper |
| leakcanary | debug only | keep debug-only |

## 3. Expensive background processes

- `SyncWorker` / `BatchSyncWorker` — scheduled **auto-sync** (AutoSync pref: hourly/daily/etc). Droid-Lite: default OFF / manual refresh only.
- `DownloadStatsWorker` — sends download stats to provider. **Remove entirely** (no network calls beyond user-initiated).
- `RBWorker` — reproducible-build verification fetch. Keep (security-relevant, opt-in).
- `DownloadWorker` / `InstallWorker` — user-initiated only. Keep.
- Boot receiver re-schedules auto sync — keep only if scheduling kept.

## 4. Feature inventory (existing)

Explore (Latest/New/Explore tabs), Search, App details (screenshots, changelog, privacy/tracker analysis, permissions, versions), Installed + Updates, Repository manager (add/edit/enable/disable, fingerprint, mirrors), Prefs (personal/layout/cache/sync/updates/installer/network/sort-filter), Onboarding flow, QR repo share, download-stats chart, Kids mode, themes (dynamic/amoled etc), installer types: Default/Root/Shizuku/Session/Legacy, anti-feature flags, RB provider check, download directory, proxy support.

## 5. Repository catalogue & index implementation

- `data/index/RepositoryUpdater.kt` — full pipeline: download (index-v1.jar / entry.jar+index-v2.json / diffs) → verify **jar signature → certificate fingerprint** → parse (v1 Jackson / v2 kotlinx) → merge → commit to Room.
- v1 + v2 both supported (v2 with per-category diffs). Catalogue of known repos exists in onboarding (`OnboardingReposPage`) as hardcoded suggestions — **not** gating custom repos.
- Universal repo support is already generic: any URL + fingerprint (optional) can be added. Must preserve.

## 6. Signature & verification mechanisms

- Index: JAR signature check (`validateCertificate`) + user-entered fingerprint compare (`calculateFingerprint`, SHA-256 of cert).
- APK: `validatePackage` — SHA-256 (or release hashType) integrity + packageName/versionCode metadata + **signer certificate must match index signature** + permission-delta check for auto-updates.
- `DisableSignatureCheck` / `DisableCertificateValidation` prefs exist — keep as expert opt-outs, default off (never default-on).

## 7. Resources

- `src/main/res` = 2.0 MB, 82 XML files, 71 locale `values-*` dirs (Weblate translations). Vectors only (no PNGs besides launcher). Fonts: none bundled.
- Bundle size dominated by DEX (Compose+Ktor+Koin+Room) and locales.

## 8. Size-reduction opportunities (hypotheses to verify by build)

1. Drop Ktor stack → keep OkHttp only.
2. Drop Vico charts, zxing, biometric, material-kolor, compose-html, markdown, jackson, simple-storage, accompanist, MDC material, androidx.preference.
3. Replace Koin with manual DI; drop koin-annotations+KSP.
4. Replace navigation3/adaptive/nav-compose with a minimal custom navigator.
5. Locale count reduction (keep top ~10) if needed as last resort — trade-off to document.
6. R8 full-mode + `android.enableR8.fullMode` default in AGP 9; check `proguard.pro` rules for over-keep.

## 9. Build environment (this machine)

- JDK 17 present (`~/jdk17`), **toolchain needs 21** → enable Gradle auto-provisioning (Adoptium) or install JDK 21.
- Android SDK `~/android-sdk`: platforms 35/36, build-tools 35/36. compileSdk 37 + build-tools 36 required → AGP auto-download if licenses accepted (they are).
- No `local.properties` yet — must point `sdk.dir`.

## 10. Risks

- Compose beta-line libs pinned via BOM — removing navigation3 may fight BOM constraints (fine, we drop the artifacts).
- F-Droid index v1 parsing via Jackson — rewrite must keep fingerprint/JAR verification intact (security-critical, no shortcuts).
- Translation stripping may break `GenerateBuildConfig` locale task — adjust if locales reduced.
