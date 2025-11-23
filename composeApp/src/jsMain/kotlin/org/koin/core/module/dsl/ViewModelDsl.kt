package org.koin.core.module.dsl

import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier

fun <T : Any> Module.viewModel(
    qualifier: Qualifier? = null,
    createdAtStart: Boolean = false,
    definition: Definition<T>
) {
    single(qualifier = qualifier, createdAtStart = createdAtStart, definition = definition)
}

