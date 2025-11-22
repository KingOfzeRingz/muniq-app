package com.doubleu.muniq.core.localization

object Localization {

    // Later can save this in datastore
    private var current: Strings = Strings_en

    fun setLanguage(lang: Language) {
        current = when (lang) {
            Language.EN -> Strings_en
            Language.DE -> Strings_de
            Language.RU -> Strings_ru
        }
    }

    val strings: Strings
        get() = current
}

enum class Language { EN, DE, RU }