package com.machiav3lli.fdroid.ui.dialog

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.ui.components.DialogNegativeButton
import com.machiav3lli.fdroid.ui.components.DialogPositiveButton
import com.machiav3lli.fdroid.utils.extension.android.Android

// Droid-Lite theme picker: five straightforward options, no custom blending.
private data class ThemeOption(
    val nightMode: Int,
    val dynamic: Boolean,
    val black: Boolean,
    val labelId: Int,
    val needsS: Boolean = false,
)

@Composable
fun ThemePrefDialogUI(
    openDialogCustom: MutableState<Boolean>,
) {
    val currentTheme = Preferences[Preferences.Key.AppTheme]
    val options = buildList {
        add(ThemeOption(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, false, false, R.string.system))
        add(ThemeOption(AppCompatDelegate.MODE_NIGHT_NO, false, false, R.string.light))
        add(ThemeOption(AppCompatDelegate.MODE_NIGHT_YES, false, false, R.string.dark))
        add(ThemeOption(AppCompatDelegate.MODE_NIGHT_YES, false, true, R.string.amoled))
        if (Android.sdk(Build.VERSION_CODES.S)) {
            add(ThemeOption(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, true, false, R.string.dynamic_theme))
            add(ThemeOption(AppCompatDelegate.MODE_NIGHT_YES, true, true, R.string.dynamic_theme))
        }
    }

    var selected by remember {
        mutableStateOf(
            options.firstOrNull {
                it.nightMode == currentTheme.nightMode &&
                        it.dynamic == currentTheme.dynamicColor &&
                        it.black == currentTheme.blackOnDark
            } ?: options.first()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(id = R.string.theme),
            style = MaterialTheme.typography.titleMedium,
        )

        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .selectable(selected = selected == option) { selected = option }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == option, onClick = { selected = option })
                Text(text = stringResource(id = option.labelId))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            DialogNegativeButton(onClick = {
                openDialogCustom.value = false
            })
            DialogPositiveButton(onClick = {
                val theme = Preferences.NeoTheme.CustomTheme(
                    resId = if (selected.black) R.style.Theme_Main_Amoled else R.style.Theme_Main,
                    nightMode = selected.nightMode,
                    dynamicColor = selected.dynamic,
                    seedColor = 0xFF00695C,
                    paletteStyle = 0,
                    blackOnDark = selected.black,
                    contrast = 0.0,
                )
                Preferences[Preferences.Key.AppTheme] = theme
                openDialogCustom.value = false
            })
        }
    }
}
