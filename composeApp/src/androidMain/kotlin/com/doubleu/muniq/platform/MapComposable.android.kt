package com.doubleu.muniq.platform

import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.doubleu.muniq.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import java.util.Locale
import org.json.JSONObject

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

/**
 * Flip this flag once you are ready to graduate from a uniform accent color to
 * automatically generated hues per district.
 */
private const val USE_DISTINCT_DISTRICT_COLORS = false
private const val MAP_LOG_TAG = "MuniqMap"
private val districtColorOverrides: Map<String, Int> = emptyMap()

@Composable
actual fun MuniqMap(
    modifier: Modifier,
    isDarkTheme: Boolean,
    onTap: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val munichCenter = LatLng(48.137154, 11.576124)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(munichCenter, 11f)
    }

    var geoJsonString by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(context) {
        geoJsonString = runCatching {
            context.resources.openRawResource(R.raw.munich_districts)
                .bufferedReader()
                .use { it.readText() }
        }.onFailure {
            Log.e(MAP_LOG_TAG, "Failed to load munich_districts.geojson", it)
        }.getOrNull()
    }

    var geoJsonLayer by remember { mutableStateOf<GeoJsonLayer?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
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
        ) {
            MapEffect(isDarkTheme, geoJsonString) { googleMap ->
                geoJsonLayer?.removeLayerFromMap()
                val jsonPayload = geoJsonString
                if (jsonPayload == null) {
                    Log.w(MAP_LOG_TAG, "Munich district data not available yet")
                    geoJsonLayer = null
                    return@MapEffect
                }

                val layer = GeoJsonLayer(googleMap, JSONObject(jsonPayload))
                layer.applyDistrictStyling(isDarkTheme)
                layer.setOnFeatureClickListener { feature ->
                    val districtName = feature.districtName()
                    Toast
                        .makeText(
                            context,
                            "District: $districtName",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
                layer.addLayerToMap()
                geoJsonLayer = layer
            }
        }
    }
}

private fun GeoJsonLayer.applyDistrictStyling(isDarkTheme: Boolean) {
    val strokeColor = if (isDarkTheme) {
        Color.argb(230, 255, 255, 255)
    } else {
        Color.argb(230, 23, 64, 17)
    }
    val uniformFillColor = if (isDarkTheme) {
        Color.argb(55, 46, 204, 113)
    } else {
        Color.argb(70, 46, 204, 113)
    }

    for (feature in features) {
        val fillColor = feature.resolveFillColor(
            uniformColor = uniformFillColor,
            fallbackAlpha = Color.alpha(uniformFillColor)
        )

        val style = GeoJsonPolygonStyle().apply {
            this.fillColor = fillColor
            this.strokeColor = strokeColor
            strokeWidth = 3.75f
            zIndex = 2.5f
            isClickable = true
        }
        feature.polygonStyle = style
    }
}

private fun GeoJsonFeature.resolveFillColor(
    uniformColor: Int,
    fallbackAlpha: Int
): Int {
    val sbNummer = getProperty("sb_nummer")
    val overrideColor = sbNummer?.let(districtColorOverrides::get)
    if (overrideColor != null) {
        return overrideColor.ensureAlpha(fallbackAlpha)
    }

    return if (USE_DISTINCT_DISTRICT_COLORS) {
        generatedColor(seedOverride = sbNummer)
    } else {
        uniformColor
    }
}

private fun GeoJsonFeature.generatedColor(seedOverride: String? = null): Int {
    val identifier = listOfNotNull(
        seedOverride,
        getProperty("sb_nummer"),
        getProperty("objectid"),
        id
    ).firstOrNull().orEmpty()
    val normalizedHash = (identifier.hashCode() % 360).let { if (it < 0) it + 360 else it }
    val hsv = floatArrayOf(normalizedHash.toFloat(), 0.55f, 0.85f)
    return Color.HSVToColor(90, hsv)
}

private fun Int.ensureAlpha(fallbackAlpha: Int): Int {
    val alpha = Color.alpha(this)
    return if (alpha == 0xFF) {
        Color.argb(
            fallbackAlpha,
            Color.red(this),
            Color.green(this),
            Color.blue(this)
        )
    } else {
        this
    }
}

private fun Feature.districtName(): String {
    return listOf(
        getProperty("name"),
        getProperty("sb_name"),
        getProperty("sb_nummer")
    ).firstOrNull { !it.isNullOrBlank() }
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        ?: "Unknown"
}