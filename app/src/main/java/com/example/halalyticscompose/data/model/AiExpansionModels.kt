package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

// ============================================================
// FASE 4: OFFLINE OCR MODELS
// ============================================================
data class HaramIngredient(
    val id: Int,
    val name: String,
    val aliases: List<String>?,
    val category: String,
    val severity: Int,
    val description: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("updated_at") val updatedAt: String? = null,
)

data class OcrSyncResponse(
    val success: Boolean,
    val message: String,
    val data: List<HaramIngredient>
)

data class OcrScanResultRequest(
    @SerializedName("product_name") val productName: String?,
    @SerializedName("raw_text") val rawText: String,
    @SerializedName("detected_haram") val detectedHaram: List<String>?,
    val severity: Int?
)

// ============================================================
// FASE 5: SMART NUTRITION MODELS
// ============================================================
data class DailyNutritionLog(
    val id: Int,
    @SerializedName("meal_type") val mealType: String,
    @SerializedName("food_items") val foodItems: List<FoodItem>?,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("total_carbs") val totalCarbs: Double,
    @SerializedName("total_protein") val totalProtein: Double,
    @SerializedName("total_fat") val totalFat: Double,
    @SerializedName("image_path") val imagePath: String?,
    @SerializedName("logged_at") val loggedAt: String,
    @SerializedName("analysis_note") val analysisNote: String? = null,
)

data class FoodItem(
    val name: String,
    @SerializedName("weight_gram") val weightGram: Int,
    val calories: Int,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    @SerializedName("is_halal") val isHalal: Boolean,
    @SerializedName("halal_note") val halalNote: String?
)

data class NutritionGoal(
    val id: Int = 0,
    @SerializedName("daily_calories") val dailyCalories: Int,
    @SerializedName("daily_carbs") val dailyCarbs: Double,
    @SerializedName("daily_protein") val dailyProtein: Double,
    @SerializedName("daily_fat") val dailyFat: Double,
    @SerializedName("goal_type") val goalType: String
)

data class NutritionDashboardResponse(
    val success: Boolean,
    val data: NutritionDashboardData
)

data class NutritionDashboardData(
    val date: String,
    val logs: List<DailyNutritionLog>,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("total_carbs") val totalCarbs: Double,
    @SerializedName("total_protein") val totalProtein: Double,
    @SerializedName("total_fat") val totalFat: Double,
    val goal: NutritionGoal?
)

data class NutritionHistoryItem(
    val date: String,
    @SerializedName("total_calories") val totalCalories: Int,
    @SerializedName("total_carbs") val totalCarbs: Double,
    @SerializedName("total_protein") val totalProtein: Double,
    @SerializedName("total_fat") val totalFat: Double,
    @SerializedName("meal_count") val mealCount: Int = 0,
)

// ============================================================
// FASE 6: RECIPE AI MODELS
// ============================================================
data class Recipe(
    val id: Int,
    val title: String,
    val description: String?,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val category: String?,
    @SerializedName("is_halal_verified") val isHalalVerified: Boolean,
    @SerializedName("image_path") val imagePath: String?,
    val user: UserCompact?
)

data class RecipeIngredient(
    val name: String,
    val amount: String?,
    val unit: String?
)

data class UserCompact(
    val id_user: Int,
    val username: String,
    @SerializedName("full_name") val fullName: String?
)

data class RecipeSubstitutionResponse(
    val success: Boolean,
    val data: SubstitutionData
)

data class SubstitutionData(
    val ingredients: List<IngredientSubstitution>,
    @SerializedName("overall_note") val overallNote: String?
)

data class IngredientSubstitution(
    val original: String,
    val status: String,
    val reason: String?,
    @SerializedName("halal_substitute") val halalSubstitute: String?,
    @SerializedName("healthy_substitute") val healthySubstitute: String?,
    @SerializedName("substitute_note") val substituteNote: String?
)

// (AR Finder models removed)



// ============================================================
// HALOCODE MODELS
// ============================================================
data class Expert(
    val id: Int,
    val specialization: String,
    val bio: String?,
    @SerializedName("is_online") val isOnline: Boolean,
    @SerializedName("price_per_session") val pricePerSession: Double,
    val rating: Float,
    @SerializedName("total_reviews") val totalReviews: Int,
    val user: UserCompactWithAvatar?
)

data class UserCompactWithAvatar(
    val id_user: Int,
    val username: String,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)
