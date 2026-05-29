package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

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

data class CategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<CategoryItem>
)

data class CategoryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("image") val image: String?
)

data class ProductDetailData(
    @SerializedName("product") val product: ProductInfo,
    @SerializedName("halal_info") val halal_info: HalalInfoResponse,
    @SerializedName("halal_source") val halal_source: String
)
