package org.koin.compose.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

@Composable
fun <T : Any> koinViewModel(
    qualifier: Qualifier? = null,
    key: String? = null,
    parameters: ParametersDefinition? = null
): T {
    return remember(qualifier, key) {
        GlobalContext.get().get(qualifier = qualifier, parameters = parameters)
    }
}

