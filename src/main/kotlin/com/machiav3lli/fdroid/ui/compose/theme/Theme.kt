package com.machiav3lli.fdroid.ui.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.isBlackTheme
import com.machiav3lli.fdroid.utils.isDynamicColorsTheme

// Droid-Lite theme: flat, fast, no custom palette blending.
// Dynamic color from wallpaper on Android 12+, fixed brand palette otherwise.
// Light / Dark / Black(AMOLED) variants only.

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    blackTheme: Boolean = isBlackTheme,
    content: @Composable () -> Unit,
) {
    val dynamic = isDynamicColorsTheme && Android.sdk(Build.VERSION_CODES.S)
    val context = LocalContext.current

    val colorScheme = when {
        dynamic && darkTheme && blackTheme -> dynamicDarkColorScheme(context).blackOut()
        dynamic && darkTheme               -> dynamicDarkColorScheme(context)
        dynamic                            -> dynamicLightColorScheme(context)
        darkTheme && blackTheme            -> DlDarkColors.blackOut()
        darkTheme                          -> DlDarkColors
        else                               -> DlLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

private fun androidx.compose.material3.ColorScheme.blackOut() = copy(
    background = Color.Black,
    surfaceContainerLowest = Color.Black,
)

private val DlLightColors = lightColorScheme(
    primary = DlPrimaryLight,
    onPrimary = DlOnPrimaryLight,
    primaryContainer = DlPrimaryContainerLight,
    onPrimaryContainer = DlOnPrimaryContainerLight,
    secondary = DlSecondaryLight,
    onSecondary = DlOnSecondaryLight,
    secondaryContainer = DlSecondaryContainerLight,
    onSecondaryContainer = DlOnSecondaryContainerLight,
    tertiary = DlTertiaryLight,
    onTertiary = DlOnTertiaryLight,
    tertiaryContainer = DlTertiaryContainerLight,
    onTertiaryContainer = DlOnTertiaryContainerLight,
    error = DlErrorLight,
    onError = DlOnErrorLight,
    errorContainer = DlErrorContainerLight,
    onErrorContainer = DlOnErrorContainerLight,
    background = DlBackgroundLight,
    onBackground = DlOnBackgroundLight,
    surface = DlSurfaceLight,
    onSurface = DlOnSurfaceLight,
    surfaceVariant = DlSurfaceVariantLight,
    onSurfaceVariant = DlOnSurfaceVariantLight,
    outline = DlOutlineLight,
    outlineVariant = DlOutlineVariantLight,
)

private val DlDarkColors = darkColorScheme(
    primary = DlPrimaryDark,
    onPrimary = DlOnPrimaryDark,
    primaryContainer = DlPrimaryContainerDark,
    onPrimaryContainer = DlOnPrimaryContainerDark,
    secondary = DlSecondaryDark,
    onSecondary = DlOnSecondaryDark,
    secondaryContainer = DlSecondaryContainerDark,
    onSecondaryContainer = DlOnSecondaryContainerDark,
    tertiary = DlTertiaryDark,
    onTertiary = DlOnTertiaryDark,
    tertiaryContainer = DlTertiaryContainerDark,
    onTertiaryContainer = DlOnTertiaryContainerDark,
    error = DlErrorDark,
    onError = DlOnErrorDark,
    errorContainer = DlErrorContainerDark,
    onErrorContainer = DlOnErrorContainerDark,
    background = DlBackgroundDark,
    onBackground = DlOnBackgroundDark,
    surface = DlSurfaceDark,
    onSurface = DlOnSurfaceDark,
    surfaceVariant = DlSurfaceVariantDark,
    onSurfaceVariant = DlOnSurfaceVariantDark,
    outline = DlOutlineDark,
    outlineVariant = DlOutlineVariantDark,
)
