package com.doubleu.muniq.feature.sidebar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MuniqSidebarContent(
    onAboutClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp))
    ) {

        Text(
            "Muniq",
            style = MaterialTheme.typography.headlineMedium
        )

        Divider()

        SidebarItem(Icons.Default.Settings, "Preferences", onPreferencesClick)
        SidebarItem(Icons.Default.Translate, "Language", onLanguageClick)
        SidebarItem(Icons.Default.Info, "About", onAboutClick)

        Spacer(modifier = Modifier.weight(1f))

        SidebarItem(Icons.Default.Refresh, "Reset Preferences", onResetClick)
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}