package com.doubleu.muniq.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = MuniqColors.MunichBlue,
    onPrimary = MuniqColors.CardBackground,
    surface = MuniqColors.CardBackground,
    onSurface = MuniqColors.BodyText,
    background = MuniqColors.BackgroundLight,
    onBackground = MuniqColors.BodyText,
    outline = MuniqColors.Border,
)

private val DarkColorScheme = darkColorScheme(
    primary = MuniqDarkColors.MunichBlue,
    background = MuniqDarkColors.Background,
    surface = MuniqDarkColors.Surface,
    onSurface = MuniqDarkColors.Text,
    onBackground = MuniqDarkColors.Text,
)


@Composable
fun MuniqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = MuniqTypography,
        shapes = MuniqShapes,
        content = content
    )
}
