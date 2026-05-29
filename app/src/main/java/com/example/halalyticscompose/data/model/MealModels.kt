package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class MealLogRequest(
    @SerializedName("product_id") val product_id: Int?,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("portion") val portion: Double,
    @SerializedName("meal_type") val meal_type: String
)

data class MealLogResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("calories") val calories: Double,
    @SerializedName("protein") val protein: Double,
    @SerializedName("fat") val fat: Double,
    @SerializedName("carbs") val carbs: Double
)

data class DailyNutritionResponse(
    @SerializedName("totals") val totals: Map<String, Double>,
    @SerializedName("logs") val logs: List<MealLogResponse>
)

data class NutritionHistoryItem(
    @SerializedName("date") val date: String,
    @SerializedName("total_calories") val total_calories: Double
)
