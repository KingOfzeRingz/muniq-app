package com.doubleu.muniq.util

import android.os.Build
import android.os.LocaleList
import com.doubleu.muniq.core.localization.Language
import java.util.Locale

actual fun getDefaultLanguage(): Language {
    val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault().get(0)
    } else {
        Locale.getDefault()
    } ?: Locale.ENGLISH

    return when (locale.language.lowercase()) {
        Language.DE.name.lowercase() -> Language.DE
        Language.RU.name.lowercase() -> Language.RU
        else -> Language.EN
    }
}

