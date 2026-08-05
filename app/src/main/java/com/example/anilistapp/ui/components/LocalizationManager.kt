package com.example.anilistapp.ui.components

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizationManager @Inject constructor() {
    private val translations = mapOf(
        "Settings" to mapOf(
            "SPANISH" to "Ajustes",
            "FRENCH" to "Paramètres",
            "JAPANESE" to "設定"
        ),
        "Save" to mapOf(
            "SPANISH" to "Guardar",
            "FRENCH" to "Sauvegarder",
            "JAPANESE" to "保存"
        ),
        "Anime" to mapOf(
            "SPANISH" to "Animé",
            "FRENCH" to "Animé",
            "JAPANESE" to "アニメ"
        ),
        "Manga" to mapOf(
            "SPANISH" to "Manga",
            "FRENCH" to "Manga",
            "JAPANESE" to "マンガ"
        ),
        "Back" to mapOf(
            "SPANISH" to "Volver",
            "FRENCH" to "Retour",
            "JAPANESE" to "戻る"
        ),
        "Synopsis" to mapOf(
            "SPANISH" to "Sinopsis",
            "FRENCH" to "Synopsis",
            "JAPANESE" to "あらすじ"
        ),
        "Trailer" to mapOf(
            "SPANISH" to "Tráiler",
            "FRENCH" to "Bande-annonce",
            "JAPANESE" to "トレーラー"
        ),
        "Request on Seerr" to mapOf(
            "SPANISH" to "Solicitar en Seerr",
            "FRENCH" to "Demander sur Seerr",
            "JAPANESE" to "Seerrでリクエスト"
        ),
        "Add to Watchlist" to mapOf(
            "SPANISH" to "Añadir a la lista",
            "FRENCH" to "Ajouter à la liste",
            "JAPANESE" to "リストに追加"
        ),
        "Search" to mapOf(
            "SPANISH" to "Buscar",
            "FRENCH" to "Rechercher",
            "JAPANESE" to "検索"
        )
    )

    fun translate(text: String, language: String): String {
        return translations[text]?.get(language) ?: text
    }

    fun getRandomTranslation(text: String, preferredLanguages: Set<String>): String {
        if (preferredLanguages.isEmpty()) return text
        val randomLang = preferredLanguages.random()
        return if (randomLang == "ENGLISH") text else translate(text, randomLang)
    }
}
