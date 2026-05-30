package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

// (DailyIntakeResponse moved to IntakeModels.kt)

data class AiInsightResponse(
    @SerializedName("status") val status: String,
    @SerializedName("insight") val insight: String
)

data class HealthScoreResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: HealthScoreData
)

data class HealthScoreData(
    val score: Int,
    val level: String,
    val color: String,
    val label: String
)

// (CategoryResponse moved to CategoryModels.kt)

// (ProductDetailData moved to HalalModels.kt)

