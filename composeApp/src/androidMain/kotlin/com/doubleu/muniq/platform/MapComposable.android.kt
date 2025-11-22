package com.doubleu.muniq.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
actual fun MuniqMap(
    modifier: Modifier,
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
                maxZoomPreference = 18f
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