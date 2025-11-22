package com.doubleu.muniq.util

import com.doubleu.muniq.core.localization.Language

actual fun getDefaultLanguage(): Language {
    // For iOS, default to English for now
    // TODO: Implement proper locale detection for iOS
    return Language.EN
}

