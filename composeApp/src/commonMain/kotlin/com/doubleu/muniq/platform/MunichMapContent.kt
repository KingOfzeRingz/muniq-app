package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.core.model.MetricType
import com.doubleu.muniq.domain.ScoreCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

private const val USE_DISTINCT_DISTRICT_COLORS = false
private val districtColorOverrides: Map<String, Int> = emptyMap()
internal const val DEFAULT_CAMERA_LATITUDE = 48.137154
internal const val DEFAULT_CAMERA_LONGITUDE = 11.576124
internal const val DEFAULT_CAMERA_ZOOM = 11f

data class GeoCoordinate(val latitude: Double, val longitude: Double)

data class GeoPolygon(val rings: List<List<GeoCoordinate>>) {
    val outer: List<GeoCoordinate> get() = rings.firstOrNull().orEmpty()
    val holes: List<List<GeoCoordinate>>
        get() =
            if (rings.size <= 1) emptyList() else rings.subList(1, rings.size)
}

data class StyledDistrict(
    val id: String,
    val name: String,
    val sbNumber: String?,
    val polygons: List<GeoPolygon>,
    val fillColor: Int,
    val strokeColor: Int,
    val sourceDistrict: District? = null
) {
    val displayName: String = name.ifBlank { sbNumber ?: "Unknown" }
}

data class MapCamera(val latitude: Double, val longitude: Double, val zoom: Float)

data class MunichMapContent(
    val camera: MapCamera,
    val districts: List<StyledDistrict>
)

@Composable
fun rememberMunichMapContent(
    isDarkTheme: Boolean,
    districtStats: List<District> = emptyList(),
    importantMetrics: List<MetricType> = emptyList(),
    ignoredMetrics: List<MetricType> = emptyList()
): MunichMapContent? {
    var contentState by remember { mutableStateOf<MunichMapContent?>(null) }
    val palette by rememberUpdatedState(MapPalette.fromTheme(isDarkTheme))

    LaunchedEffect(isDarkTheme, districtStats, importantMetrics, ignoredMetrics) {
        val geometries = MunichDistrictRepository.load()
        val styledDistricts = geometries.map { geometry ->
            val matchedDistrict = matchDistrictForGeometry(geometry, districtStats)
            StyledDistrict(
                id = geometry.id,
                name = geometry.name.ifBlank {
                    geometry.sbNumber?.let { "District $it" } ?: "Unknown"
                },
                sbNumber = geometry.sbNumber,
                polygons = geometry.polygons,
                fillColor = colorForDistrict(
                    geometry = geometry,
                    district = matchedDistrict,
                    palette = palette,
                    importantMetrics = importantMetrics,
                    ignoredMetrics = ignoredMetrics
                ),
                strokeColor = palette.strokeColor,
                sourceDistrict = matchedDistrict
            )
        }

        contentState = MunichMapContent(
            camera = MapCamera(
                latitude = DEFAULT_CAMERA_LATITUDE,
                longitude = DEFAULT_CAMERA_LONGITUDE,
                zoom = DEFAULT_CAMERA_ZOOM
            ),
            districts = styledDistricts
        )
    }

    return contentState
}

private fun colorForDistrict(
    geometry: DistrictGeometry,
    district: District?,
    palette: MapPalette,
    importantMetrics: List<MetricType>,
    ignoredMetrics: List<MetricType>
): Int {
    if (district == null) {
        return palette.fillColorFor(geometry)
    }

    val personalizedScore = ScoreCalculator.calculatePersonalizedScore(
        scores = district.scores,
        importantMetrics = importantMetrics,
        ignoredMetrics = ignoredMetrics
    )
    return calculateColor(personalizedScore)
}

private fun matchDistrictForGeometry(
    geometry: DistrictGeometry,
    districtStats: List<District>
): District? {
    geometry.sbNumber?.let { sbNumber ->
        val withoutLeadingZeros = sbNumber.trimStart('0').ifEmpty { "0" }
        districtStats.firstOrNull { stat ->
            stat.id == sbNumber ||
                    stat.id == withoutLeadingZeros ||
                    stat.id == withoutLeadingZeros.toIntOrNull()?.toString()
        }?.let { return it }
    }

    if (geometry.name.isNotBlank()) {
        val normalizedGeoName = normalizeDistrictName(geometry.name)
        districtStats.firstOrNull { normalizeDistrictName(it.name) == normalizedGeoName }?.let {
            return it
        }
    }

    return null
}

private fun normalizeDistrictName(name: String): String {
    return name.trim()
        .lowercase()
        .replace("-", " ")
        .replace("_", " ")
        .replace(Regex("\\s+"), " ")
}

private fun calculateColor(score: Int): Int {
    val clampedScore = score.coerceIn(0, 100)
    val hue = (clampedScore / 100f) * 120f
    return hsvToColor(
        alpha = 200,
        hue = hue,
        saturation = 0.85f,
        value = 0.95f
    )
}

private object MunichDistrictRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cached: List<DistrictGeometry>? = null

    suspend fun load(): List<DistrictGeometry> {
        cached?.let { return it }
        return mutex.withLock {
            cached?.let { return it }
            val districts = parseGeoJson(readGeoJson())
            cached = districts
            districts
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readGeoJson(): String = withContext(Dispatchers.Default) {
        resource("files/munich_districts.json")
            .readBytes()
            .decodeToString()
    }

    private fun parseGeoJson(raw: String): List<DistrictGeometry> {
        val root = json.parseToJsonElement(raw).jsonObject
        val features = root["features"]?.jsonArray ?: return emptyList()
        return features.mapIndexedNotNull { index, element ->
            val featureObject = element.jsonObject
            val geometry = featureObject["geometry"]?.jsonObject ?: return@mapIndexedNotNull null
            val type =
                geometry["type"]?.jsonPrimitive?.contentOrNull ?: return@mapIndexedNotNull null
            val coordinates = geometry["coordinates"] ?: return@mapIndexedNotNull null
            val polygons = when (type) {
                "Polygon" -> listOfNotNull(coordinates.jsonArray.toPolygon())
                "MultiPolygon" -> coordinates.jsonArray.mapNotNull { it.jsonArray.toPolygon() }
                else -> emptyList()
            }
            if (polygons.isEmpty()) return@mapIndexedNotNull null
            val properties = featureObject["properties"]?.jsonObject
            DistrictGeometry(
                id = featureObject["id"]?.jsonPrimitive?.content ?: "feature-$index",
                name = properties?.get("name")?.jsonPrimitive?.contentOrNull.orEmpty(),
                sbNumber = properties?.get("sb_nummer")?.jsonPrimitive?.contentOrNull,
                polygons = polygons
            )
        }
    }
}

private fun JsonArray.toPolygon(): GeoPolygon? {
    val rings = mapNotNull { ringElement ->
        ringElement.jsonArray.mapNotNull { coord ->
            coord.jsonArray.toCoordinate()
        }.takeIf { it.isNotEmpty() }
    }
    return rings.takeIf { it.isNotEmpty() }?.let { GeoPolygon(it) }
}

private fun JsonArray.toCoordinate(): GeoCoordinate? {
    if (size < 2) return null
    val longitude = this[0].jsonPrimitive.doubleOrNull ?: return null
    val latitude = this[1].jsonPrimitive.doubleOrNull ?: return null
    return GeoCoordinate(latitude = latitude, longitude = longitude)
}

private data class DistrictGeometry(
    val id: String,
    val name: String,
    val sbNumber: String?,
    val polygons: List<GeoPolygon>
)

private data class MapPalette(
    val strokeColor: Int,
    val uniformFillColor: Int
) {
    fun fillColorFor(feature: DistrictGeometry): Int {
        val override = feature.sbNumber
            ?.let(districtColorOverrides::get)
            ?.ensureAlpha((uniformFillColor ushr 24) and 0xFF)
        if (override != null) return override
        return if (USE_DISTINCT_DISTRICT_COLORS) {
            generatedColor(feature.sbNumber ?: feature.id)
        } else {
            uniformFillColor
        }
    }

    companion object {
        fun fromTheme(isDarkTheme: Boolean): MapPalette {
            val stroke = if (isDarkTheme) {
                argb(230, 255, 255, 255)
            } else {
                argb(230, 23, 64, 17)
            }
            val fill = if (isDarkTheme) {
                argb(150, 46, 204, 113)
            } else {
                argb(170, 46, 204, 113)
            }
            return MapPalette(strokeColor = stroke, uniformFillColor = fill)
        }
    }
}

private fun generatedColor(seed: String): Int {
    val hash = seed.hashCode().let { if (it < 0) it * -1 else it }
    val hue = hash % 360
    val saturation = 0.55f
    val value = 0.85f
    return hsvToColor(alpha = 90, hue = hue.toFloat(), saturation = saturation, value = value)
}

private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
    return ((alpha and 0xFF) shl 24) or
            ((red and 0xFF) shl 16) or
            ((green and 0xFF) shl 8) or
            (blue and 0xFF)
}

private fun hsvToColor(alpha: Int, hue: Float, saturation: Float, value: Float): Int {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
    val m = value - c
    val rgb = when (hue.toInt()) {
        in 0..59 -> Triple(c, x, 0f)
        in 60..119 -> Triple(x, c, 0f)
        in 120..179 -> Triple(0f, c, x)
        in 180..239 -> Triple(0f, x, c)
        in 240..299 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val r = ((rgb.first + m) * 255).toInt()
    val g = ((rgb.second + m) * 255).toInt()
    val b = ((rgb.third + m) * 255).toInt()
    return argb(alpha, r, g, b)
}

private fun Int.ensureAlpha(fallbackAlpha: Int): Int {
    val currentAlpha = this ushr 24
    return if (currentAlpha == 0xFF) {
        (fallbackAlpha shl 24) or (this and 0x00FFFFFF)
    } else {
        this
    }
}
package com.doubleu.muniq.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi
import muniq.composeapp.generated.resources.Res
import com.doubleu.muniq.core.model.District

private const val USE_DISTINCT_DISTRICT_COLORS = false
private val districtColorOverrides: Map<String, Int> = emptyMap()
internal const val DEFAULT_CAMERA_LATITUDE = 48.137154
internal const val DEFAULT_CAMERA_LONGITUDE = 11.576124
internal const val DEFAULT_CAMERA_ZOOM = 11f

data class GeoCoordinate(val latitude: Double, val longitude: Double)

data class GeoPolygon(val rings: List<List<GeoCoordinate>>) {
    val outer: List<GeoCoordinate> get() = rings.firstOrNull().orEmpty()
    val holes: List<List<GeoCoordinate>>
        get() =
            if (rings.size <= 1) emptyList() else rings.subList(1, rings.size)
}

data class StyledDistrict(
    val id: String,
    val name: String,
    val sbNumber: String?,
    val polygons: List<GeoPolygon>,
    val fillColor: Int,
    val strokeColor: Int
) {
    val displayName: String = name.ifBlank { sbNumber ?: "Unknown" }
}

data class MapCamera(val latitude: Double, val longitude: Double, val zoom: Float)

data class MunichMapContent(
    val camera: MapCamera,
    val districts: List<StyledDistrict>
)

@Composable
fun rememberMunichMapContent(
    isDarkTheme: Boolean,
    districtStats: List<District> = emptyList(),
    importantMetrics: List<com.doubleu.muniq.core.model.MetricType> = emptyList(),
    ignoredMetrics: List<com.doubleu.muniq.core.model.MetricType> = emptyList()
): MunichMapContent? {
    var contentState by remember { mutableStateOf<MunichMapContent?>(null) }
    val palette by rememberUpdatedState(MapPalette.fromTheme(isDarkTheme))

    LaunchedEffect(isDarkTheme, districtStats, importantMetrics, ignoredMetrics) {
        val districts = MunichDistrictRepository.load()
        val hasStats = districtStats.isNotEmpty()

        // DEBUG: Print what we have
        println("🔍 DEBUG: districtStats.size = ${districtStats.size}")
        println("🔍 DEBUG: importantMetrics = $importantMetrics")
        println("🔍 DEBUG: ignoredMetrics = $ignoredMetrics")
        districtStats.take(3).forEach { stat ->
            println("🔍 DEBUG: API District - id='${stat.id}', name='${stat.name}', score=${stat.overallScore}")
        }

        contentState = MunichMapContent(
            camera = MapCamera(
                latitude = DEFAULT_CAMERA_LATITUDE,
                longitude = DEFAULT_CAMERA_LONGITUDE,
                zoom = DEFAULT_CAMERA_ZOOM
            ),
            districts = districts.map { geometry ->
                // DEBUG: Print GeoJSON data
                println("🗺️ DEBUG: GeoJSON - id='${geometry.id}', sbNumber='${geometry.sbNumber}', name='${geometry.name}'")

                // Match by sb_nummer (from GeoJSON) to id (from API)
                val stats = if (hasStats && geometry.sbNumber != null) {
                    val found = districtStats.find { stat ->
                        val match1 = stat.id == geometry.sbNumber
                        val match2 = stat.id == geometry.sbNumber.toIntOrNull()?.toString()

                        // DEBUG: Print matching attempts
                        println("  🔎 Trying to match: stat.id='${stat.id}' with sbNumber='${geometry.sbNumber}'")
                        println("     match1 (direct): $match1, match2 (via int): $match2")

                        match1 || match2
                    }

                    if (found != null) {
                        println("  ✅ MATCHED! score=${found.overallScore}")
                    } else {
                        println("  ❌ NO MATCH FOUND")
                    }

                    found
                } else null

                // Calculate color based on whether we have stats
                val color = if (stats != null) {
                    // DYNAMIC CALCULATION HERE using importance-based weights
                    val dynamicScore =
                        com.doubleu.muniq.domain.ScoreCalculator.calculatePersonalizedScore(
                            scores = stats.scores,
                            importantMetrics = importantMetrics,
                            ignoredMetrics = ignoredMetrics
                        )
                    println("  🎨 Dynamic Score: $dynamicScore")
                    calculateColor(dynamicScore)  // Use the new dynamic score for color
                } else {
                    // Use default palette color only if we don't have any stats loaded yet
                    palette.fillColorFor(geometry)
                }

                StyledDistrict(
                    id = geometry.sbNumber ?: geometry.id,
                    name = geometry.name.ifBlank {
                        geometry.sbNumber?.let { "District $it" } ?: "Unknown"
                    },
                    sbNumber = geometry.sbNumber,
                    polygons = geometry.polygons,
                    fillColor = color,
                    strokeColor = palette.strokeColor
                )
            }
        )
    }

    return contentState
}

/**
 * Normalizes district names for better matching:
 * - Converts to lowercase
 * - Removes extra spaces
 * - Replaces hyphens and underscores with spaces
 */
private fun normalizeDistrictName(name: String): String {
    return name.trim()
        .lowercase()
        .replace("-", " ")
        .replace("_", " ")
        .replace(Regex("\\s+"), " ")
}

/**
 * Calculates color based on score (0-100).
 * 0-20: Red
 * 20-40: Orange
 * 40-60: Yellow
 * 60-80: Light Green
 * 80-100: Dark Green
 */
private fun calculateColor(score: Int): Int {
    // Ensure score is in valid range
    val clampedScore = score.coerceIn(0, 100)

    // Map score to hue: 0 (red) -> 120 (green)
    val hue = (clampedScore / 100f) * 120f

    // Higher saturation and value for better visibility
    return hsvToColor(
        alpha = 200,  // More opaque for better visibility
        hue = hue,
        saturation = 0.85f,  // High saturation for vibrant colors
        value = 0.95f  // High brightness
    )
}

private object MunichDistrictRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var cached: List<DistrictGeometry>? = null

    suspend fun load(): List<DistrictGeometry> {
        cached?.let { return it }
        return mutex.withLock {
            cached?.let { return it }
            val districts = parseGeoJson(readGeoJson())
            cached = districts
            districts
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun readGeoJson(): String = withContext(Dispatchers.Default) {
        // Note: Ensure "files/munich_districts.json" exists in composeApp/src/commonMain/composeResources/files/
        Res.readBytes("files/munich_districts.json").decodeToString()
    }

    private fun parseGeoJson(raw: String): List<DistrictGeometry> {
        val root = json.parseToJsonElement(raw).jsonObject
        val features = root["features"]?.jsonArray ?: return emptyList()
        return features.mapIndexedNotNull { index, element ->
            val featureObject = element.jsonObject
            val geometry = featureObject["geometry"]?.jsonObject ?: return@mapIndexedNotNull null
            val type =
                geometry["type"]?.jsonPrimitive?.contentOrNull ?: return@mapIndexedNotNull null
            val coordinates = geometry["coordinates"] ?: return@mapIndexedNotNull null
            val polygons = when (type) {
                "Polygon" -> listOfNotNull(coordinates.jsonArray.toPolygon())
                "MultiPolygon" -> coordinates.jsonArray.mapNotNull { it.jsonArray.toPolygon() }
                else -> emptyList()
            }
            if (polygons.isEmpty()) return@mapIndexedNotNull null
            val properties = featureObject["properties"]?.jsonObject
            DistrictGeometry(
                id = featureObject["id"]?.jsonPrimitive?.content ?: "feature-$index",
                name = properties?.get("name")?.jsonPrimitive?.contentOrNull.orEmpty(),
                sbNumber = properties?.get("sb_nummer")?.jsonPrimitive?.contentOrNull,
                polygons = polygons
            )
        }
    }
}

private fun JsonArray.toPolygon(): GeoPolygon? {
    val rings = mapNotNull { ringElement ->
        ringElement.jsonArray.mapNotNull { coord ->
            coord.jsonArray.toCoordinate()
        }.takeIf { it.isNotEmpty() }
    }
    return rings.takeIf { it.isNotEmpty() }?.let { GeoPolygon(it) }
}

private fun JsonArray.toCoordinate(): GeoCoordinate? {
    if (size < 2) return null
    val longitude = this[0].jsonPrimitive.doubleOrNull ?: return null
    val latitude = this[1].jsonPrimitive.doubleOrNull ?: return null
    return GeoCoordinate(latitude = latitude, longitude = longitude)
}

private data class DistrictGeometry(
    val id: String,
    val name: String,
    val sbNumber: String?,
    val polygons: List<GeoPolygon>
)

private data class MapPalette(
    val strokeColor: Int,
    val uniformFillColor: Int
) {
    fun fillColorFor(feature: DistrictGeometry): Int {
        val override = feature.sbNumber
            ?.let(districtColorOverrides::get)
            ?.ensureAlpha((uniformFillColor ushr 24) and 0xFF)
        if (override != null) return override
        return if (USE_DISTINCT_DISTRICT_COLORS) {
            generatedColor(feature.sbNumber ?: feature.id)
        } else {
            uniformFillColor
        }
    }

    companion object {
        fun fromTheme(isDarkTheme: Boolean): MapPalette {
            val stroke = if (isDarkTheme) {
                argb(230, 255, 255, 255)
            } else {
                argb(230, 23, 64, 17)
            }
            val fill = if (isDarkTheme) {
                argb(150, 255, 20, 147)  // Pink (DeepPink) for testing
            } else {
                argb(150, 255, 105, 180)  // HotPink for testing
            }
            return MapPalette(strokeColor = stroke, uniformFillColor = fill)
        }
    }
}

private fun DistrictGeometry.toStyledDistrict(palette: MapPalette): StyledDistrict {
    return StyledDistrict(
        id = id,
        name = name.ifBlank { sbNumber?.let { "District $it" } ?: "Unknown" },
        sbNumber = sbNumber,
        polygons = polygons,
        fillColor = palette.fillColorFor(this),
        strokeColor = palette.strokeColor
    )
}

private fun generatedColor(seed: String): Int {
    val hash = seed.hashCode().let { if (it < 0) it * -1 else it }
    val hue = hash % 360
    val saturation = 0.55f
    val value = 0.85f
    return hsvToColor(alpha = 90, hue = hue.toFloat(), saturation = saturation, value = value)
}

private fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
    return ((alpha and 0xFF) shl 24) or
            ((red and 0xFF) shl 16) or
            ((green and 0xFF) shl 8) or
            (blue and 0xFF)
}

private fun hsvToColor(alpha: Int, hue: Float, saturation: Float, value: Float): Int {
    val c = value * saturation
    val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
    val m = value - c
    val rgb = when (hue.toInt()) {
        in 0..59 -> Triple(c, x, 0f)
        in 60..119 -> Triple(x, c, 0f)
        in 120..179 -> Triple(0f, c, x)
        in 180..239 -> Triple(0f, x, c)
        in 240..299 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val r = ((rgb.first + m) * 255).toInt()
    val g = ((rgb.second + m) * 255).toInt()
    val b = ((rgb.third + m) * 255).toInt()
    return argb(alpha, r, g, b)
}

private fun Int.ensureAlpha(fallbackAlpha: Int): Int {
    val currentAlpha = this ushr 24
    return if (currentAlpha == 0xFF) {
        (fallbackAlpha shl 24) or (this and 0x00FFFFFF)
    } else {
        this
    }
}

private fun JsonObject?.propertyOrNull(key: String): String? =
    this?.get(key)?.jsonPrimitive?.contentOrNull