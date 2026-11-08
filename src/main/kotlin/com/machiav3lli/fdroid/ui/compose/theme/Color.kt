package com.machiav3lli.fdroid.ui.compose.theme

import androidx.compose.ui.graphics.Color

// ── Droid-Lite brand palette ──────────────────────────────────────────────
// Independent visual identity: signal teal-green on neutral greys.

// Light
val DlPrimaryLight = Color(0xFF00695C)
val DlOnPrimaryLight = Color(0xFFFFFFFF)
val DlPrimaryContainerLight = Color(0xFFB2DFDB)
val DlOnPrimaryContainerLight = Color(0xFF00251E)
val DlSecondaryLight = Color(0xFF376561)
val DlOnSecondaryLight = Color(0xFFFFFFFF)
val DlSecondaryContainerLight = Color(0xFFBCECF6).let { Color(0xFFB9E9E3) }
val DlOnSecondaryContainerLight = Color(0xFF00201D)
val DlTertiaryLight = Color(0xFF4A6079)
val DlOnTertiaryLight = Color(0xFFFFFFFF)
val DlTertiaryContainerLight = Color(0xFFD2E4FF)
val DlOnTertiaryContainerLight = Color(0xFF001D33)

val DlErrorLight = Color(0xFFBA1A1A)
val DlOnErrorLight = Color(0xFFFFFFFF)
val DlErrorContainerLight = Color(0xFFFFDAD6)
val DlOnErrorContainerLight = Color(0xFF410002)
val DlBackgroundLight = Color(0xFFFAFDFB)
val DlOnBackgroundLight = Color(0xFF191C1B)
val DlSurfaceLight = Color(0xFFFAFDFB)
val DlOnSurfaceLight = Color(0xFF191C1B)
val DlSurfaceVariantLight = Color(0xFFDAE5E1)
val DlOnSurfaceVariantLight = Color(0xFF3F4947)
val DlOutlineLight = Color(0xFF6F7977)
val DlOutlineVariantLight = Color(0xFFBEC9C5)

// Dark
val DlPrimaryDark = Color(0xFF54DBC4)
val DlOnPrimaryDark = Color(0xFF003730)
val DlPrimaryContainerDark = Color(0xFF005046)
val DlOnPrimaryContainerDark = Color(0xFF76F8E0)
val DlSecondaryDark = Color(0xFF9ECCC6)
val DlOnSecondaryDark = Color(0xFF043631)
val DlSecondaryContainerDark = Color(0xFF214E49)
val DlOnSecondaryContainerDark = Color(0xFFBBE8E2)
val DlTertiaryDark = Color(0xFFB2C8E8)
val DlOnTertiaryDark = Color(0xFF1B324A)
val DlTertiaryContainerDark = Color(0xFF334861)
val DlOnTertiaryContainerDark = Color(0xFFD2E4FF)

val DlErrorDark = Color(0xFFFFB4AB)
val DlOnErrorDark = Color(0xFF690005)
val DlErrorContainerDark = Color(0xFF93000A)
val DlOnErrorContainerDark = Color(0xFFFFDAD6)
val DlBackgroundDark = Color(0xFF191C1B)
val DlOnBackgroundDark = Color(0xFFE1E3E1)
val DlSurfaceDark = Color(0xFF191C1B)
val DlOnSurfaceDark = Color(0xFFE1E3E1)
val DlSurfaceVariantDark = Color(0xFF3F4947)
val DlOnSurfaceVariantDark = Color(0xFFBEC9C5)
val DlOutlineDark = Color(0xFF89938F)
val DlOutlineVariantDark = Color(0xFF3F4947)

// Privacy meter accents (kept from upstream semantics, new tones)
val StrongRed = Color(0xFFE53935)
val LightRedOrange = Color(0xFFFFB59D)
val LightYellowGreen = Color(0xFFC6E1A5)
val PaleYellow = Color(0xFFFFF0B3)
val TealGreen = Color(0xFF4DB6AC)
