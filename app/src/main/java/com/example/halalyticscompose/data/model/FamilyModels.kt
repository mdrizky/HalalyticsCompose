package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class FamilyProfile(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("relationship")
    val relationship: String?,
    @SerializedName("age")
    val age: Int?,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("weight_kg")
    val weightKg: Double?,
    @SerializedName("height_cm")
    val heightCm: Double?,
    @SerializedName("activity_level")
    val activityLevel: String?,
    @SerializedName("daily_calories_target")
    val dailyCaloriesTarget: Int?,
    @SerializedName("daily_sugar_limit_g")
    val dailySugarLimitG: Double?,
    @SerializedName("daily_sodium_limit_mg")
    val dailySodiumLimitMg: Int?,
    @SerializedName("daily_fat_limit_g")
    val dailyFatLimitG: Double?,
    @SerializedName("allergies")
    val allergies: String?,
    @SerializedName("medical_history")
    val medicalHistory: String?,
    @SerializedName("image_path")
    val imagePath: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

data class FamilyListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<FamilyProfile>
)

data class FamilyDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: FamilyProfile
)
