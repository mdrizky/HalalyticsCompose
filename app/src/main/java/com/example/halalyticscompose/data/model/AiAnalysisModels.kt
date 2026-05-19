package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request model for AI Analysis
 */
data class AiAnalysisRequest(
    @SerializedName("ingredients_text")
    val ingredientsText: String,
    
    @SerializedName("user_profile")
    val userProfile: Map<String, Any>? = null,

    @SerializedName("family_id")
    val familyId: Int? = null
)

/**
 * Response model for AI Analysis
 */
data class AiAnalysisResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("content")
    val content: AiAnalysisContent? = null,
    
    @SerializedName("message")
    val message: String? = null
)

data class AiAnalysisContent(
    @SerializedName("status")
    val status: String, // Halal, Haram, Syubhat, Unknown
    
    @SerializedName("confidence")
    val confidence: Int = 0,
    
    @SerializedName("analysis")
    val analysis: String = "", // Detailed explanation

    @SerializedName("ringkasan")
    val ringkasan: String? = null,
    
    @SerializedName("red_flags")
    val redFlags: List<String> = emptyList(),
    
    @SerializedName("health_risk")
    val healthRisk: String = "safe" // safe, low, high
)

data class BmiAdviceRequest(
    @SerializedName("weight_kg") val weightKg: Float,
    @SerializedName("height_cm") val heightCm: Float
)

data class BmiAdviceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: BmiAdviceData? = null,
    @SerializedName("message") val message: String? = null
)

data class BmiAdviceData(
    @SerializedName("status_fisik") val statusFisik: String,
    @SerializedName("target_2_bulan") val target2Bulan: String,
    @SerializedName("saran_nutrisi") val saranNutrisi: List<String>,
    @SerializedName("saran_olahraga") val saranOlahraga: List<String>,
    @SerializedName("pesan_motivasi") val pesanMotivasi: String
)
