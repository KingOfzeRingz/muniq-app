package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.doubleu.muniq.core.model.District
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLScriptElement

private const val GOOGLE_MAPS_WEB_API_KEY = "YOUR_WEB_GOOGLE_MAPS_API_KEY"
private const val MAP_SCRIPT_ID = "google-maps-script"

@Suppress("UNUSED_PARAMETER")
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
    val latestContent by rememberUpdatedState(mapContent)
    val latestOnTap by rememberUpdatedState(onTap)
    var selectedDistrict by remember { mutableStateOf<District?>(null) }
    val containerId = remember { "muniq-map-${Random.nextInt(0, Int.MAX_VALUE)}" }

    Div(attrs = {
        id(containerId)
        style {
            height(100.percent)
        }
    })

    if (selectedDistrict != null) {
        Div(attrs = {
            style {
                property("position", "absolute")
                property("bottom", "16px")
                property("left", "16px")
                property("padding", "8px 12px")
                property("background-color", "rgba(0,0,0,0.6)")
                property("color", "#ffffff")
                property("border-radius", "8px")
                property("font-family", "sans-serif")
            }
        }) { Text("District: ${selectedDistrict?.name}") }
    }

    LaunchedEffect(latestContent, isDarkTheme) {
        val content = latestContent ?: return@LaunchedEffect
        GoogleMapsBridge.ensureLoaded()
        GoogleMapsBridge.render(
            containerId = containerId,
            content = content,
            isDarkTheme = isDarkTheme,
            onMapTap = latestOnTap,
            onPolygonTap = { district ->
                selectedDistrict = district
                onDistrictClick(district)
            }
        )
    }
}

private object GoogleMapsBridge {
    private var mapInstance: dynamic = null
    private val overlays = mutableListOf<dynamic>()
    private var scriptLoading = false

    suspend fun ensureLoaded() {
        if (isLibraryReady()) return
        if (scriptLoading) {
            waitUntilReady()
            return
        }
        scriptLoading = true
        loadScript()
        waitUntilReady()
    }

    private fun isLibraryReady(): Boolean =
        js("typeof window.google !== 'undefined' && window.google.maps") as Boolean

    private suspend fun waitUntilReady() {
        while (!isLibraryReady()) {
            delay(50)
        }
    }

    private suspend fun loadScript() = suspendCancellableCoroutine { continuation ->
        val head = document.head ?: document.body ?: run {
            continuation.resumeWithException(IllegalStateException("Cannot access DOM head"))
            return@suspendCancellableCoroutine
        }
        val existing = document.getElementById(MAP_SCRIPT_ID)
        if (existing != null) {
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }
        val script = document.createElement("script") as HTMLScriptElement
        script.id = MAP_SCRIPT_ID
        script.async = true
        script.defer = true
        script.src = "https://maps.googleapis.com/maps/api/js?key=$GOOGLE_MAPS_WEB_API_KEY"
        script.onload = {
            continuation.resume(Unit)
        }
        script.onerror = {
            continuation.resumeWithException(RuntimeException("Unable to load Google Maps JS API"))
        }
        head.appendChild(script)
    }

    fun render(
        containerId: String,
        content: MunichMapContent,
        isDarkTheme: Boolean,
        onMapTap: (Double, Double) -> Unit,
        onPolygonTap: (District?) -> Unit
    ) {
        val container = document.getElementById(containerId) ?: return
        if (mapInstance == null) {
            mapInstance = js("new google.maps.Map")(
                container,
                jsObject {
                    zoom = content.camera.zoom
                    center = latLngLiteral(content.camera.latitude, content.camera.longitude)
                    disableDefaultUI = true
                }
            )
            addDomListener(mapInstance, "click") { event ->
                val lat = event.latLng?.lat()
                val lng = event.latLng?.lng()
                if (lat is Double && lng is Double) {
                    onMapTap(lat, lng)
                }
            }
        } else {
            mapInstance.setOptions(
                jsObject {
                    zoom = content.camera.zoom
                    center = latLngLiteral(content.camera.latitude, content.camera.longitude)
                }
            )
        }

        applyTheme(isDarkTheme)
        clearOverlays()

        content.districts.forEach { district ->
            district.polygons.forEach { polygon ->
                val paths = mutableListOf<dynamic>()
                paths.add(polygon.outer.toJsPath())
                polygon.holes.forEach { hole -> paths.add(hole.toJsPath()) }
                val polygonOptions = jsObject {
                    this.paths = paths.toTypedArray()
                    this.strokeColor = district.strokeColor.toCssColor()
                    this.strokeWeight = 3.75
                    this.strokeOpacity = district.strokeColor.alphaComponent()
                    this.fillColor = district.fillColor.toCssColor()
                    this.fillOpacity = district.fillColor.alphaComponent()
                    this.clickable = true
                }
                val jsPolygon = js("new google.maps.Polygon")(polygonOptions)
                jsPolygon.setMap(mapInstance)
                addDomListener(jsPolygon, "click") {
                    WebToast.show("District: ${district.displayName}")
                    onPolygonTap(district.sourceDistrict)
                }
                overlays.add(jsPolygon)
            }
        }
    }

    private fun clearOverlays() {
        overlays.forEach { overlay ->
            overlay.setMap(null)
        }
        overlays.clear()
    }

    private fun applyTheme(isDarkTheme: Boolean) {
        if (!isDarkTheme) {
            mapInstance?.setOptions(jsObject { styles = null })
            return
        }
        val styles = js("JSON.parse")(DARK_MAP_STYLE_JSON)
        mapInstance?.setOptions(jsObject { this.styles = styles })
    }
}

private fun List<GeoCoordinate>.toJsPath(): Array<dynamic> =
    map { latLngLiteral(it.latitude, it.longitude) }.toTypedArray()

private fun latLngLiteral(lat: Double, lng: Double): dynamic =
    jsObject {
        this.lat = lat
        this.lng = lng
    }

private fun Int.toCssColor(): String {
    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    val a = ((this ushr 24) and 0xFF) / 255.0
    return "rgba($r,$g,$b,$a)"
}

private fun Int.alphaComponent(): Double =
    ((this ushr 24) and 0xFF) / 255.0

private fun jsObject(builder: dynamic.() -> Unit): dynamic {
    val obj = js("({})")
    builder(obj)
    return obj
}

private fun addDomListener(target: dynamic, eventName: String, listener: (dynamic) -> Unit) {
    js("google.maps.event.addListener")(target, eventName, listener)
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

private object WebToast {
    fun show(message: String) {
        val body = document.body ?: return
        val toast = document.createElement("div") as HTMLDivElement
        toast.textContent = message
        toast.setAttribute(
            "style",
            """
                position:fixed;
                bottom:24px;
                left:50%;
                transform:translateX(-50%);
                background:rgba(0,0,0,0.7);
                color:white;
                padding:10px 16px;
                border-radius:12px;
                font-family:sans-serif;
                font-size:14px;
                opacity:0;
                transition:opacity 150ms ease-in-out;
            """.trimIndent()
        )
        body.appendChild(toast)
        window.setTimeout({
            toast.style.opacity = "1"
        }, 10)
        window.setTimeout({
            toast.style.opacity = "0"
            window.setTimeout({
                body.removeChild(toast)
            }, 150)
        }, 1800)
    }
}


