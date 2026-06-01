package com.example.halalyticscompose.data.model

data class ProductIntelligenceResult(
    val barcode: String,
    val name: String,
    val brands: String?,
    val imageUrl: String?,
    val ingredientsText: String?,
    val halalAnalysis: HalalAnalysisResult,
    val healthScore: HealthScoreResult,
    val ingredientsAnalysis: List<IngredientDetail>?,
    val alternatives: List<AlternativeProduct>?,
    val nutriments: OFFNutriments?,
    val novaGroup: Int?,
    val nutriscoreGrade: String?
)

data class HalalAnalysisResult(
    val status: IntelligenceHalalStatus,
    val reason: String,
    val suspiciousIngredients: List<String>
)

data class HealthScoreResult(
    val score: Int, // 0-100
    val grade: String, // SEHAT, CUKUP SEHAT, TIDAK SEHAT
    val factors: List<HealthFactor>
)

data class HealthFactor(
    val name: String,
    val value: String,
    val impact: String, // "+5", "-10", dll.
    val description: String
)

data class IngredientDetail(
    val name: String,
    val function: String,
    val risk: String,
    val level: String // "AMAN", "PERHATIAN", "HINDARI"
)

data class AlternativeProduct(
    val name: String,
    val reason: String,
    val tips: String,
    val halal: String
)

data class OFFNutriments(
    val energyKcal: Double?,
    val fat: Double?,
    val saturatedFat: Double?,
    val carbohydrates: Double?,
    val sugars: Double?,
    val fiber: Double?,
    val proteins: Double?,
    val salt: Double?,
    val sodium: Double?
)
