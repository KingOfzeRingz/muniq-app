package com.doubleu.muniq.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = MuniqColors.MunichBlue,
    onPrimary = Color.White,
    primaryContainer = MuniqColors.MunichBlueContainer,
    onPrimaryContainer = MuniqColors.MunichBlueContainerText,
    secondary = MuniqColors.MunichBlue,
    onSecondary = Color.White,
    secondaryContainer = MuniqColors.MunichBlueSoft,
    onSecondaryContainer = MuniqColors.MunichBlueContainerText,
    surface = MuniqColors.CardBackground,
    onSurface = MuniqColors.BodyText,
    background = MuniqColors.BackgroundLight,
    onBackground = MuniqColors.BodyText,
    surfaceVariant = MuniqColors.MunichBlueSoft,
    outline = MuniqColors.Border
)

private val DarkColorScheme = darkColorScheme(
    primary = MuniqDarkColors.MunichBlue,
    onPrimary = Color.White,
    primaryContainer = MuniqDarkColors.MunichBlueContainer,
    onPrimaryContainer = MuniqDarkColors.MunichBlueContainerText,
    secondary = MuniqDarkColors.MunichBlue,
    onSecondary = Color.White,
    secondaryContainer = MuniqDarkColors.SurfaceVariant,
    onSecondaryContainer = MuniqDarkColors.Text,
    surface = MuniqDarkColors.Surface,
    onSurface = MuniqDarkColors.Text,
    background = MuniqDarkColors.Background,
    onBackground = MuniqDarkColors.Text,
    surfaceVariant = MuniqDarkColors.SurfaceVariant,
    outline = MuniqDarkColors.Border
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
