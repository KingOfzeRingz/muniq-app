package com.doubleu.muniq.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private const val DARK_MAP_STYLE_JSON = """
[
  {"elementType":"geometry","stylers":[{"color":"#1f1f1f"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#90caf9"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#1f1f1f"}]},
  {"featureType":"administrative","elementType":"geometry","stylers":[{"color":"#212121"}]},
  {"featureType":"poi","elementType":"labels.text.fill","stylers":[{"color":"#e0e0e0"}]},
  {"featureType":"poi.park","elementType":"geometry.fill","stylers":[{"color":"#1b5e20"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#2c2c2c"}]},
  {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#b0bec5"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#0f172a"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#82b1ff"}]}
]
"""

@Composable
actual fun MuniqMap(
    modifier: Modifier,
    isDarkTheme: Boolean,
    onTap: (Double, Double) -> Unit
) {
    val munichCenter = LatLng(48.137154, 11.576124)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(munichCenter, 11f)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = false,
                isTrafficEnabled = false,
                isIndoorEnabled = false,
                isBuildingEnabled = true,
                minZoomPreference = 10f,
                maxZoomPreference = 18f,
                mapStyleOptions = if (isDarkTheme) MapStyleOptions(DARK_MAP_STYLE_JSON) else null
            ),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = false,
                indoorLevelPickerEnabled = false
            ),
            onMapClick = { latLng ->
                onTap(latLng.latitude, latLng.longitude)
            }
        )

    }
}