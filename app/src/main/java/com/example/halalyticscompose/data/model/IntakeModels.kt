package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

/**
 * Updated Daily Intake Models to match Laravel UserController response.
 */
data class DailyIntakeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("daily_intake") val dailyIntake: DailyIntakeData? = null,
    @SerializedName("targets") val targets: IntakeTargets? = null,
    @SerializedName("progress") val progress: IntakeProgress? = null
)

data class DailyIntakeData(
    @SerializedName("total_water_ml") val totalWaterMl: Int = 0,
    @SerializedName("total_caffeine_mg") val totalCaffeineMg: Int = 0,
    @SerializedName("total_sugar_g") val totalSugarG: Int = 0,
    @SerializedName("total_calories") val totalCalories: Int = 0,
    @SerializedName("total_sodium_mg") val totalSodiumMg: Int = 0,
    @SerializedName("total_carbs_g") val totalCarbsG: Int = 0,
    @SerializedName("total_protein_g") val totalProteinG: Int = 0,
    @SerializedName("total_fat_g") val totalFatG: Int = 0
)

data class IntakeTargets(
    @SerializedName("water_target_ml") val waterTargetMl: Int = 2000,
    @SerializedName("caffeine_limit_mg") val caffeineLimitMg: Int = 400,
    @SerializedName("calorie_limit") val calorieLimit: Int = 2000,
    @SerializedName("sugar_limit_g") val sugarLimitG: Int = 50,
    @SerializedName("sodium_limit_mg") val sodiumLimitMg: Int = 2300,
    @SerializedName("carbs_target_g") val carbsTargetG: Int = 275,
    @SerializedName("protein_target_g") val proteinTargetG: Int = 50,
    @SerializedName("fat_target_g") val fatTargetG: Int = 70
)

data class IntakeProgress(
    @SerializedName("water_percentage") val waterPercentage: Float,
    @SerializedName("caffeine_percentage") val caffeinePercentage: Float
)
