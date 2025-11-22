package com.doubleu.muniq.core.model

enum class MetricType(val displayName: String, val key: String) {
    RENT("Rent affordability", "rent"),
    GREEN("Green Index", "green"),
    CHILD("Child friendly", "family"),
    STUDENT("Student friendly", "mobility"), // Mapping to mobility for now
    QUIET("Quietness", "quiet"),
    AIR("Air Quality", "air"),
    BIKE("Bike friendly", "bike"),
    DENSITY("Population Density", "density")
}

data class UserPreference(
    val type: MetricType,
    val isImportant: Boolean
)
