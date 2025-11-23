package com.doubleu.muniq.core.localization

interface Strings {
    val appName: String
    val nav_home: String
    val nav_map: String
    val nav_discover: String
    val district_details: String

    val settings_title: String
    val language_section_label: String
    val appearance_section_label: String
    val language_option_english: String
    val language_option_german: String
    val language_option_russian: String
    val theme_option_system: String
    val theme_option_light: String
    val theme_option_dark: String
    val sidebar_settings: String
    val sidebar_about: String

    // Common labels
    val green_space: String
    val quietness: String
    val air_quality: String
    val mobility: String
    val family: String

    // Priority Sheet
    val priority_sheet_title: String
    val priority_sheet_description: String
    val priority_active_section: String
    val priority_ignored_section: String
    val priority_empty_state: String
    val priority_update_map_button: String

    // District Detail
    val district_detail_personalized_score: String
    val district_detail_metrics_breakdown: String
    val district_detail_priority_badge: String // Format with %d
    val district_detail_ignored_badge: String

    // Metric Types
    val metric_rent: String
    val metric_green: String
    val metric_child: String
    val metric_student: String
    val metric_quiet: String
    val metric_air: String
    val metric_bike: String
    val metric_density: String

    // Score Labels
    val score_excellent: String
    val score_good: String
    val score_average: String
    val score_below_average: String
    val score_poor: String
}