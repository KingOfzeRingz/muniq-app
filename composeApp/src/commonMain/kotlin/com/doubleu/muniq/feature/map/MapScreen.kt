package com.doubleu.muniq.feature.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.doubleu.muniq.core.designsystem.MuniqColors
import com.doubleu.muniq.platform.MuniqMap

@Composable
fun MapScreen(
    onMenuClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onFabClick: () -> Unit = {},
    onMapTap: (Double, Double) -> Unit = { _, _ -> }
) {
    Box(Modifier.fillMaxSize()) {

        // --- MAP -----------------------------------------------------------
        MuniqMap(
            modifier = Modifier.fillMaxSize(),
            onTap = onMapTap
        )

        // --- SAFE AREA ------------------------------------------------------
        val safeArea = WindowInsets.safeDrawing.asPaddingValues()

        // --- TOP LEFT BUTTON (MENU) ----------------------------------------
        MuniqCircleIconButton(
            icon = Icons.Rounded.Menu,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(safeArea)
                .padding(12.dp),
            onClick = onMenuClick
        )

        // --- TOP RIGHT BUTTON (FILTER) -------------------------------------
        MuniqCircleIconButton(
            icon = Icons.Rounded.Tune,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(safeArea)
                .padding(12.dp),
            onClick = onFilterClick
        )
//
//        // --- FLOATING ACTION BUTTON (FAB) ----------------------------------
//        FloatingActionButton(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(safeArea)
//                .padding(bottom = 24.dp),
//            containerColor = MuniqColors.MunichBlue,
//            onClick = onFabClick
//        ) {
//            Text("✨", color = Color.White)
//        }
    }
}

@Composable
fun MuniqCircleIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        color = containerColor
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}