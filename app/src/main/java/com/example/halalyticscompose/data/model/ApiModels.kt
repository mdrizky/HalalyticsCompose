package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * Health and Intake related models
 */
data class DailyIntakeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: Map<String, Double>
)

data class AiInsightResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("insight") val insight: String
)

data class HealthScoreResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("score") val score: Int,
    @SerializedName("status") val status: String
)

data class ProductDetailData(
    @SerializedName("product") val product: ProductInfo,
    @SerializedName("halal_info") val halal_info: HalalInfoResponse,
    @SerializedName("halal_source") val halal_source: String
)

/**
 * Generic API Wrapper for consistency
 */
data class GenericApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)
