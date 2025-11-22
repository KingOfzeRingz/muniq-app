package com.doubleu.muniq.app.navigation

import Navigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.doubleu.muniq.feature.districtdetail.DistrictDetailScreen
import com.doubleu.muniq.feature.map.MapScreen
import com.doubleu.muniq.feature.onboarding.OnboardingScreen

@Composable
fun NavGraph(navigator: Navigator) {
    val screen by navigator.current.collectAsState()

    when (val s = screen) {
        Screen.Onboarding -> OnboardingScreen(
            onFinished = { navigator.navigate(Screen.Map) }
        )
        Screen.Map -> MapScreen(
            onDistrictSelected = { id ->
                navigator.navigate(Screen.DistrictDetail(id))
            }
        )
        is Screen.DistrictDetail -> DistrictDetailScreen(
            districtId = s.districtId,
            onBack = navigator::backToMap
        )
    }
}