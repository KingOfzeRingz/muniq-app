package com.doubleu.muniq.core.localization

object Localization {

    // Later can save this in datastore
    private var currentStrings: Strings = Strings_en
    private var currentLanguage: Language = Language.EN

    fun setLanguage(lang: Language) {
        currentLanguage = lang
        currentStrings = when (lang) {
            Language.EN -> Strings_en
            Language.DE -> Strings_de
            Language.RU -> Strings_ru
        }
    }

    val strings: Strings
        get() = currentStrings

    val language: Language
        get() = currentLanguage
}

enum class Language(val displayName: String) {
    EN("English"),
    DE("Deutsch"),
    RU("Русский")
}