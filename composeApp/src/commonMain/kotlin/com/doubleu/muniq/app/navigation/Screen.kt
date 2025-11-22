package com.doubleu.muniq.app.navigation

sealed class Screen {
    data object Onboarding : Screen()
    data object Map : Screen()
    data class DistrictDetail(val districtId: String) : Screen()
}