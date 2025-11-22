package com.doubleu.muniq.app

import Navigator
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.doubleu.muniq.app.navigation.NavGraph
import com.doubleu.muniq.core.designsystem.MuniqTheme
import com.doubleu.muniq.core.localization.Localization
import com.doubleu.muniq.core.localization.Strings
import com.doubleu.muniq.feature.settings.ThemePreference
import com.doubleu.muniq.util.getDefaultLanguage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun MuniqApp() {
    var themePreference by rememberSaveable { mutableStateOf(ThemePreference.SYSTEM) }
    var selectedLanguage by rememberSaveable { mutableStateOf(getDefaultLanguage()) }
    var currentStrings by remember { mutableStateOf<Strings>(Localization.strings) }
    val systemDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> systemDarkTheme
    }

    // Update strings when language changes
    currentStrings = when (selectedLanguage) {
        com.doubleu.muniq.core.localization.Language.EN -> com.doubleu.muniq.core.localization.Strings_en
        com.doubleu.muniq.core.localization.Language.DE -> com.doubleu.muniq.core.localization.Strings_de
        com.doubleu.muniq.core.localization.Language.RU -> com.doubleu.muniq.core.localization.Strings_ru
    }

    MuniqTheme(darkTheme = isDarkTheme) {
        val navigator = remember { Navigator() }
        NavGraph(
            navigator = navigator,
            darkTheme = isDarkTheme,
            themePreference = themePreference,
            onThemePreferenceChange = { themePreference = it },
            currentLanguage = selectedLanguage,
            onLanguageSelected = { selectedLanguage = it },
            strings = currentStrings
        )
    }
}