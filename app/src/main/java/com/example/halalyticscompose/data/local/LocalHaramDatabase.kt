package com.example.halalyticscompose.data.local

import com.example.halalyticscompose.data.model.HalalAnalysisResult
import com.example.halalyticscompose.data.model.IntelligenceHalalStatus

object LocalHaramDatabase {

    private val haramList = listOf(
        "babi", "pork", "lard", "bacon", "ham", "gelatin babi",
        "alkohol", "alcohol", "wine", "beer", "rum", "mirin", "sake",
        "darah", "blood", "carmine", "e120", "karmin", "enzim babi",
        "pepsin", "rennet babi"
    )

    private val syubhatList = listOf(
        "gelatin", "e471", "e472", "e472a", "e472b", "e472c", "e472d", "e472e", "e472f",
        "lesitin", "whey", "casein", "kasein", "perisa alami", "natural flavor",
        "l-cysteine", "e920", "shortening", "margarin", "gliserin", "glycerol", "e422",
        "kolagen", "collagen", "tallow"
    )

    fun analyzeIngredients(ingredientsText: String?): HalalAnalysisResult? {
        if (ingredientsText.isNullOrBlank()) return null

        val textLower = ingredientsText.lowercase()
        
        // 1. Cek Haram
        val haramFound = haramList.filter { textLower.contains(it) }
        if (haramFound.isNotEmpty()) {
            return HalalAnalysisResult(
                status = IntelligenceHalalStatus.HARAM,
                reason = "Ditemukan bahan yang jelas haram.",
                suspiciousIngredients = haramFound
            )
        }

        // 2. Cek Syubhat
        val syubhatFound = syubhatList.filter { textLower.contains(it) }
        if (syubhatFound.isNotEmpty()) {
            return HalalAnalysisResult(
                status = IntelligenceHalalStatus.SYUBHAT,
                reason = "Ditemukan bahan yang meragukan (syubhat) dan memerlukan sertifikasi halal resmi.",
                suspiciousIngredients = syubhatFound
            )
        }

        // 3. Jika aman dari DB lokal
        return HalalAnalysisResult(
            status = IntelligenceHalalStatus.HALAL,
            reason = "Bahan-bahan terdeteksi aman dari database lokal.",
            suspiciousIngredients = emptyList()
        )
    }
}
