# Droid-Lite — Transformation Plan

Baseline: stock Neo Store release APK ≈ 6 MB (to be verified by our own build).

## Phase 1 — Identity
- applicationId `com.machiav3lli.fdroid` → `com.droidlite.client`; app name "Droid-Lite"; new launcher icon; new colour system.
- Internal Kotlin package stays `com.machiav3lli.fdroid` (documented decision: package renames across 350 files risk reflection/Koin breakage with zero APK-size benefit; branding lives in applicationId/label/UI).
- Preserve COPYING (GPLv3) + upstream attribution in README.

## Phase 2 — Dependency reduction (size-first, low-risk order)
Remove entirely:
1. `kotlinx-datetime` → `java.time` (desugared already)
2. `markdown` (already unused)
3. `zxing` (QR share) — feature removed
4. `vico` charts + `DownloadStatsWorker` + DownloadStats DAOs/screens — stats fetch = unnecessary network
5. `material-kolor` → platform dynamicColor
6. `compose-html` → minimal HTML→AnnotatedString
7. `jackson` → kotlinx-serialization in v1 parser
8. `biometric` — feature removed
9. `simple-storage` → SAF intents directly
10. `accompanist-permissions` → ActivityResult API
11. MDC `material` (views) — Compose-only now
12. `androidx.preference` — custom prefs storage already exists
13. Ktor (4 artifacts) → OkHttp directly in Downloader + Coil networker
14. `navigation3` + `navigation-compose` + `adaptive` → ~100-line custom back stack (keep nav3's NavKey idea)
15. `koin-compose/startup/annotations` if unused after VM changes

Keep: Room, WorkManager, OkHttp, Coil(core+compose+okhttp), Koin core/android(+workmanager), serialization, coroutines, lifecycle, compose core+m3, libsu, shizuku, hiddenapi-bypass, desugar.

## Phase 3 — Feature removal report
- QR repo sharing, download-stats charts+fetch, biometric lock, Legacy installer (pre-minSdk), Kids mode UI extras, multi-theme engine (keep light/dark/black + dynamic), exodus dead table, "Neo" flavour overlay dirs.
- Keep ALL security: jar/cert fingerprint, APK hash+signature+metadata checks, RB verification (opt-in), permission-delta gate.

## Phase 4 — UI replacement (new system, not recolour)
- Flat compact design: dense lists, top app bar, 3 tabs (Latest/Explore/Installed) via simple pager, plain Search screen, card-less app rows, minimal App details (header, actions, links, versions, changelog, privacy note), plain Prefs (list style), plain Repos screen.
- Custom ~100-line navigator replacing NavDisplay; no bottom-sheet scenes (plain dialogs); no navigation-suite scaffold (custom compact bottom bar).
- New theme: single Theme.kt, light/dark/black, dynamic color on 12+.
- Icons: curated vector set (~30 icons), delete 12k-LOC phosphor set.
- Locale trimming via `resourceConfigurations` (~12 languages) — documented trade-off.

## Phase 5 — Build & measure loop
assembleRelease after each phase; record APK bytes + APK Analyzer breakdown (dex/resources).

## Phase 6 — Verify & document
Security checklist (index fingerprint, APK validation paths untouched + unit-tested), README rewrite, AUDIT/plan docs.
