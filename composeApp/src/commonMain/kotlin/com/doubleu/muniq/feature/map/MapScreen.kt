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
import com.doubleu.muniq.feature.sidebar.MuniqSidebarContent
import com.doubleu.muniq.feature.sidebar.SidebarLayout
import com.doubleu.muniq.feature.sidebar.DrawerState
import com.doubleu.muniq.platform.MuniqMap

import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MapScreen(
    drawerState: DrawerState = remember { DrawerState() },
    onFilterClick: () -> Unit = {},
    onMapTap: (Double, Double) -> Unit = { _, _ -> },
    onOpenSettings: () -> Unit = {},
    isDarkTheme: Boolean = false,
    strings: com.doubleu.muniq.core.localization.Strings = com.doubleu.muniq.core.localization.Localization.strings,
    importantMetrics: List<com.doubleu.muniq.core.model.MetricType> = emptyList(),
    ignoredMetrics: List<com.doubleu.muniq.core.model.MetricType> = emptyList(),
    viewModel: MapViewModel = koinViewModel()
) {
    val districts by viewModel.districts.collectAsState()

    // State for selected district
    var selectedDistrict by remember { mutableStateOf<com.doubleu.muniq.core.model.District?>(null) }

    SidebarLayout(
        drawerState = drawerState,
        drawerContent = {
            MuniqSidebarContent(
                strings = strings,
                onAboutClick = {},
                onPreferencesClick = onOpenSettings,
                onLanguageClick = {},
                onResetClick = {}
            )
        }
    ) {

        Box(Modifier.fillMaxSize()) {

            MuniqMap(
                modifier = Modifier.fillMaxSize(),
                isDarkTheme = isDarkTheme,
                districts = districts,
                importantMetrics = importantMetrics,
                ignoredMetrics = ignoredMetrics,
                onTap = onMapTap,
                onDistrictClick = { district ->
                    selectedDistrict = district
                }
            )

            val safeArea = WindowInsets.safeDrawing.asPaddingValues()

            MuniqCircleIconButton(
                icon = Icons.Rounded.Menu,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(safeArea)
                    .padding(12.dp),
                onClick = { drawerState.open() }
            )

            MuniqCircleIconButton(
                icon = Icons.Rounded.Tune,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(safeArea)
                    .padding(12.dp),
                onClick = onFilterClick
            )
        }

        // Show district detail bottom sheet when a district is selected
        selectedDistrict?.let { district ->
            DistrictDetailBottomSheet(
                district = district,
                importantMetrics = importantMetrics,
                ignoredMetrics = ignoredMetrics,
                strings = strings,
                onDismiss = { selectedDistrict = null }
            )
        }
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
