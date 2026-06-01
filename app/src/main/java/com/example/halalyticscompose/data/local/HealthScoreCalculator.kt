package com.example.halalyticscompose.data.local

import com.example.halalyticscompose.data.model.HealthFactor
import com.example.halalyticscompose.data.model.HealthScoreResult
import com.example.halalyticscompose.data.model.OFFNutriments

object HealthScoreCalculator {

    fun calculate(
        novaGroup: Int?,
        nutriscoreGrade: String?,
        nutriments: OFFNutriments?
    ): HealthScoreResult {
        var score = 100
        val factors = mutableListOf<HealthFactor>()

        // 1. Nova Group (Tingkat Pemrosesan)
        if (novaGroup != null) {
            when (novaGroup) {
                1 -> {
                    factors.add(HealthFactor("Nova Group 1", "Alami/Minim Proses", "0", "Produk alami, sangat baik."))
                }
                2 -> {
                    score -= 5
                    factors.add(HealthFactor("Nova Group 2", "Bahan Kuliner", "-5", "Produk olahan ringan."))
                }
                3 -> {
                    score -= 15
                    factors.add(HealthFactor("Nova Group 3", "Makanan Olahan", "-15", "Telah melalui proses pengolahan."))
                }
                4 -> {
                    score -= 30
                    factors.add(HealthFactor("Nova Group 4", "Ultra-Proses", "-30", "Sangat banyak bahan tambahan dan pengolahan industri."))
                }
            }
        }

        // 2. Nutri-Score (Profil Gizi)
        if (nutriscoreGrade != null) {
            when (nutriscoreGrade.lowercase()) {
                "a" -> {
                    score += 5
                    factors.add(HealthFactor("Nutri-Score A", "Sangat Baik", "+5", "Kualitas nutrisi terbaik."))
                }
                "b" -> {
                    factors.add(HealthFactor("Nutri-Score B", "Baik", "0", "Kualitas nutrisi baik."))
                }
                "c" -> {
                    score -= 10
                    factors.add(HealthFactor("Nutri-Score C", "Cukup", "-10", "Kualitas nutrisi standar rata-rata."))
                }
                "d" -> {
                    score -= 20
                    factors.add(HealthFactor("Nutri-Score D", "Buruk", "-20", "Mengandung gizi yang kurang seimbang."))
                }
                "e" -> {
                    score -= 30
                    factors.add(HealthFactor("Nutri-Score E", "Sangat Buruk", "-30", "Sangat tidak disarankan konsumsi rutin."))
                }
            }
        }

        // 3. Nutriments (Kandungan Spesifik per 100g)
        nutriments?.let { nut ->
            // Gula
            if (nut.sugars != null) {
                if (nut.sugars > 30.0) {
                    score -= 20
                    factors.add(HealthFactor("Gula Tinggi", "${nut.sugars}g", "-20", "Kadar gula sangat tinggi (di atas 30g)."))
                } else if (nut.sugars > 15.0) {
                    score -= 10
                    factors.add(HealthFactor("Gula Sedang", "${nut.sugars}g", "-10", "Kadar gula cukup tinggi."))
                }
            }

            // Lemak Jenuh
            if (nut.saturatedFat != null && nut.saturatedFat > 10.0) {
                score -= 15
                factors.add(HealthFactor("Lemak Jenuh", "${nut.saturatedFat}g", "-15", "Kandungan lemak jenuh tinggi."))
            }

            // Garam
            if (nut.salt != null && nut.salt > 2.5) {
                score -= 15
                factors.add(HealthFactor("Garam Tinggi", "${nut.salt}g", "-15", "Garam (Natrium) tinggi, risiko hipertensi."))
            }

            // Serat
            if (nut.fiber != null) {
                if (nut.fiber > 6.0) {
                    score += 10
                    factors.add(HealthFactor("Tinggi Serat", "${nut.fiber}g", "+10", "Sangat baik untuk pencernaan."))
                } else if (nut.fiber > 3.0) {
                    score += 5
                    factors.add(HealthFactor("Sumber Serat", "${nut.fiber}g", "+5", "Cukup untuk kesehatan usus."))
                }
            }

            // Protein
            if (nut.proteins != null && nut.proteins > 10.0) {
                score += 5
                factors.add(HealthFactor("Sumber Protein", "${nut.proteins}g", "+5", "Sumber protein yang baik."))
            }
        }

        // Batasi skor antara 0-100
        val finalScore = score.coerceIn(0, 100)
        
        val grade = when {
            finalScore >= 70 -> "SEHAT"
            finalScore >= 45 -> "CUKUP SEHAT"
            else -> "TIDAK SEHAT"
        }

        return HealthScoreResult(
            score = finalScore,
            grade = grade,
            factors = factors
        )
    }
}
