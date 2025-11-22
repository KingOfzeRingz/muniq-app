package com.doubleu.muniq.feature.sidebar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun SidebarLayout(
    drawerState: DrawerState,
    drawerWidth: Int = 280,
    drawerContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) {
    val isOpen by drawerState.isOpen.collectAsState()

    val offsetX by animateDpAsState(
        targetValue = if (isOpen) 0.dp else (-drawerWidth).dp,
        animationSpec = androidx.compose.animation.core.spring()
    )

    val insets = WindowInsets.safeDrawing.asPaddingValues()

    Box(Modifier.fillMaxSize()) {

        content()

        if (isOpen) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable { drawerState.close() }
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth.dp)
                .offset(x = offsetX)
                .padding(
                    top = insets.calculateTopPadding(),
                    bottom = insets.calculateBottomPadding()
                ),
            shape = RoundedCornerShape(
                topEnd = 32.dp,
                bottomEnd = 32.dp
            ),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top
            ) {
                drawerContent()
            }
        }

    }
}