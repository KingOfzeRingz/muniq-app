package com.doubleu.muniq.app.navigation

import Navigator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.doubleu.muniq.core.localization.Language
import com.doubleu.muniq.core.localization.Strings
import com.doubleu.muniq.feature.map.MapScreen
import com.doubleu.muniq.feature.settings.SettingsScreen
import com.doubleu.muniq.feature.settings.ThemePreference
import com.doubleu.muniq.feature.splash.SplashScreen

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.doubleu.muniq.feature.priorities.PrioritySheet

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(
    navigator: Navigator,
    darkTheme: Boolean,
    themePreference: ThemePreference,
    onThemePreferenceChange: (ThemePreference) -> Unit,
    currentLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    strings: Strings
) {
    val screen by navigator.current.collectAsState()
    var showPrioritySheet by remember { mutableStateOf(false) }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = screen,
        transitionSpec = {
            val slideIn = slideInHorizontally(
                animationSpec = tween(320),
                initialOffsetX = { fullWidth ->
                    if (targetState is Screen.Settings) fullWidth else -fullWidth
                }
            ) + fadeIn(animationSpec = tween(200))

            val slideOut = slideOutHorizontally(
                animationSpec = tween(280),
                targetOffsetX = { fullWidth ->
                    if (targetState is Screen.Settings) -fullWidth else fullWidth
                }
            ) + fadeOut(animationSpec = tween(200))

            slideIn with slideOut
        }
    ) { destination ->
        when (destination) {
            Screen.Map -> {
                MapScreen(
                    isDarkTheme = darkTheme,
                    strings = strings,
                    onOpenSettings = { navigator.navigate(Screen.Settings) },
                    onFilterClick = { showPrioritySheet = true },
                    onMapTap = { lat, lng ->
                        // later convert lat/lng → district
                    }
                )
                
                if (showPrioritySheet) {
                    PrioritySheet(
                        strings = strings,
                        onDismiss = { showPrioritySheet = false }
                    )
                }
            }
            Screen.Settings -> SettingsScreen(
                strings = strings,
                currentLanguage = currentLanguage,
                onLanguageSelected = onLanguageSelected,
                themePreference = themePreference,
                onThemePreferenceChange = onThemePreferenceChange,
                onBack = navigator::backToMap
            )
            Screen.Splash -> SplashScreen(
                onNavigateToMap = { navigator.navigate(Screen.Map) }
            )
        }
    }
}