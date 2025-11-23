@file:OptIn(ExperimentalForeignApi::class)

package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.doubleu.muniq.core.model.District
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCameraUpdate
import cocoapods.GoogleMaps.GMSMapStyle
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapViewDelegateProtocol
import cocoapods.GoogleMaps.GMSMutablePath
import cocoapods.GoogleMaps.GMSOverlay
import cocoapods.GoogleMaps.GMSPolygon
import cocoapods.GoogleMaps.kGMSTypeNormal
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreLocation.CLLocationCoordinate2D
import platform.Foundation.NSError
import platform.Foundation.NSMutableArray
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionBeginFromCurrentState
import platform.UIKit.UIWindow
import platform.UIKit.animateWithDuration
import platform.darwin.NSObject

@Composable
actual fun MuniqMap(
    modifier: Modifier,
    isDarkTheme: Boolean,
    districts: List<District>,
    importantMetrics: List<com.doubleu.muniq.core.model.MetricType>,
    ignoredMetrics: List<com.doubleu.muniq.core.model.MetricType>,
    onTap: (latitude: Double, longitude: Double) -> Unit,
    onDistrictClick: (District?) -> Unit
) {
    val mapContent = rememberMunichMapContent(isDarkTheme, districts, importantMetrics, ignoredMetrics)
    val latestMapContent by rememberUpdatedState(mapContent)
    val latestOnTap by rememberUpdatedState(onTap)
    val delegate = remember { MuniqMapDelegate() }

    LaunchedEffect(isDarkTheme) {
        delegate.isDarkTheme = isDarkTheme
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val camera = GMSCameraPosition.cameraWithLatitude(
                latitude = DEFAULT_CAMERA_LATITUDE,
                longitude = DEFAULT_CAMERA_LONGITUDE,
                zoom = DEFAULT_CAMERA_ZOOM
            )
            GMSMapView.mapWithFrame(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), camera = camera).apply {
                settings.compassButton = false
                settings.indoorPicker = false
                settings.myLocationButton = false
                settings.scrollGestures = true
                settings.zoomGestures = true
                settings.rotateGestures = false
                settings.tiltGestures = false
                mapType = kGMSTypeNormal
                this.delegate = delegate
            }
        },
        update = { mapView ->
            delegate.onMapTap = latestOnTap
            delegate.onDistrictTap = { district ->
                onDistrictClick(district)
            }
            mapView.applyTheme(isDarkTheme)
            mapView.updateCamera(latestMapContent?.camera)
            mapView.renderDistricts(latestMapContent)
        }
    )
}

private fun GMSMapView.renderDistricts(content: MunichMapContent?) {
    clear()
    val districts = content?.districts ?: return
    districts.forEach { district ->
        district.polygons.forEach { polygon ->
            val path = GMSMutablePath.path().apply {
                polygon.outer.forEach { coord ->
                    addLatitude(coord.latitude, longitude = coord.longitude)
                }
            }
            val overlay = GMSPolygon().apply {
                this.path = path
                strokeColor = district.strokeColor.toUIColor()
                fillColor = district.fillColor.toUIColor()
                strokeWidth = 3.75
                tappable = true
                userData = DistrictOverlayPayload(district.displayName, district.sourceDistrict)
                holes = polygon.holes.toHolesArray() as List<*>?
            }
            overlay.map = this
        }
    }
}

private fun List<List<GeoCoordinate>>.toHolesArray(): NSMutableArray? {
    if (isEmpty()) return null
    val array = NSMutableArray()
    forEach { ring ->
        val path = GMSMutablePath.path()
        ring.forEach { coord ->
            path.addLatitude(coord.latitude, longitude = coord.longitude)
        }
        array.addObject(path)
    }
    return array
}

private fun GMSMapView.updateCamera(camera: MapCamera?) {
    camera ?: return
    val target = GMSCameraPosition.cameraWithLatitude(
        latitude = camera.latitude,
        longitude = camera.longitude,
        zoom = camera.zoom
    )
    val update = cocoapods.GoogleMaps.GMSCameraUpdate.setCamera(target)
    moveCamera(update)
}

private fun GMSMapView.applyTheme(isDarkTheme: Boolean) {
    if (!isDarkTheme) {
        mapStyle = null
        return
    }
    memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()
        val style = GMSMapStyle.styleWithJSONString(
            DARK_MAP_STYLE_JSON,
            error = errorPtr.ptr
        )
        if (style != null) {
            mapStyle = style
        }
    }
}

private fun Int.toUIColor(): UIColor {
    val alpha = ((this ushr 24) and 0xFF).toDouble() / 255.0
    val red = ((this ushr 16) and 0xFF).toDouble() / 255.0
    val green = ((this ushr 8) and 0xFF).toDouble() / 255.0
    val blue = (this and 0xFF).toDouble() / 255.0
    return UIColor.colorWithRed(red, green = green, blue = blue, alpha = alpha)
}

private class MuniqMapDelegate : NSObject(), GMSMapViewDelegateProtocol {
    var onMapTap: ((Double, Double) -> Unit)? = null
    var onDistrictTap: ((District?) -> Unit)? = null
    var isDarkTheme: Boolean = false

    override fun mapView(mapView: GMSMapView, didTapAtCoordinate: CValue<CLLocationCoordinate2D>) {
        didTapAtCoordinate.useContents {
            onMapTap?.invoke(latitude, longitude)
        }
    }

    override fun mapView(mapView: GMSMapView, didTapOverlay: GMSOverlay) {
        val payload = didTapOverlay.userData as? DistrictOverlayPayload ?: return
        presentToast(payload.displayName)
        onDistrictTap?.invoke(payload.district)
    }
}

private data class DistrictOverlayPayload(
    val displayName: String,
    val district: District?
)

private fun presentToast(message: String) {
    val window = activeWindow() ?: return
    val width = window.bounds.useContents { size.width }
    val height = window.bounds.useContents { size.height }
    val label = UILabel(
        frame = CGRectMake(
            32.0,
            height - 120.0,
            width - 64.0,
            44.0
        )
    ).apply {
        backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.7)
        textColor = UIColor.whiteColor
        font = UIFont.systemFontOfSize(15.0)
        textAlignment = NSTextAlignmentCenter
        numberOfLines = 2
        text = "District: $message"
        layer.cornerRadius = 12.0
        layer.masksToBounds = true
        alpha = 0.0
    }
    window.addSubview(label)

    UIView.animateWithDuration(
        0.25,
        animations = {
            label.alpha = 1.0
        }
    )
    UIView.animateWithDuration(
        0.4,
        delay = 1.2,
        options = UIViewAnimationOptionBeginFromCurrentState,
        animations = {
            label.alpha = 0.0
        },
        completion = { _ ->
            label.removeFromSuperview()
        }
    )
}

private fun activeWindow(): UIWindow? {
    val application = UIApplication.sharedApplication
    application.keyWindow?.let { return it }
    val windows = application.windows
    val count = windows.size
    for (index in 0 until count) {
        val candidate = windows[index] as? UIWindow
        if (candidate?.isKeyWindow() == true) {
            return candidate
        }
    }
    return null
}

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