package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

// UI Models
data class Product(
    val id: Int,
    val barcode: String,
    val name: String,
    val brand: String = "Unknown",
    val category: String = "Unknown",
    val image: String? = null,
    val halalInfo: HalalInfo? = null,
    // Open Food Facts Data
    val nutriScore: String? = null,
    val ingredientsText: String? = null,
    val allergens: List<String>? = null,
    val traces: List<String>? = null,
    val additives: List<String>? = null,
    val nutrientLevels: NutrientLevels? = null,
    val nutritionFacts: NutritionFacts? = null,
    val quantity: String? = null,
    val servingSize: String? = null,
    val countries: List<String>? = null,
    val packaging: String? = null,
    val stores: String? = null,
    val brandsTags: List<String>? = null,
    val categoriesTags: List<String>? = null,
    val labelsTags: List<String>? = null,
    val ingredientsAnalysisTags: List<String>? = null,
    val novaGroup: Int? = null,
    val novaGroupName: String? = null,
    val uniqueScansCount: Int? = null,
    val lastModified: String? = null,
    val created: String? = null,
    val imageUrl: String? = null,
    val imageFrontUrl: String? = null,
    val imageIngredientsUrl: String? = null,
    val imageNutritionUrl: String? = null,
    val halalNotes: String? = null,
    val ingredientsTags: List<String>? = null,
    val aiConfidence: AIConfidence? = null,
    val isVerified: Boolean = false,
    val verificationStatus: String? = "needs_review",
    val aiSummary: String? = null
)

data class AIConfidence(
    val score: Double,
    val level: String, // "high", "medium", "low"
    val message: String
)

data class NutrientLevels(
    val fat: NutrientLevel?,
    val saturatedFat: NutrientLevel?,
    val sugars: NutrientLevel?,
    val salt: NutrientLevel?,
    val energy: NutrientLevel?,
    val fiber: NutrientLevel?,
    val proteins: NutrientLevel?,
    val alcohol: NutrientLevel?
)

data class NutrientLevel(
    val level: String, // "high", "moderate", "low"
    val value: Double,
    val unit: String,
    val percentOfDailyNeeds: Double?
)

data class NutritionFacts(
    val energy: NutritionItem?,
    val fat: NutritionItem?,
    val saturatedFat: NutritionItem?,
    val carbohydrates: NutritionItem?,
    val sugars: NutritionItem?,
    val fiber: NutritionItem?,
    val proteins: NutritionItem?,
    val salt: NutritionItem?,
    val sodium: NutritionItem?,
    val alcohol: NutritionItem?,
    val vitaminA: NutritionItem?,
    val vitaminD: NutritionItem?,
    val vitaminE: NutritionItem?,
    val vitaminK: NutritionItem?,
    val vitaminC: NutritionItem?,
    val vitaminB1: NutritionItem?,
    val vitaminB2: NutritionItem?,
    val vitaminB6: NutritionItem?,
    val vitaminB9: NutritionItem?,
    val vitaminB12: NutritionItem?,
    val calcium: NutritionItem?,
    val iron: NutritionItem?,
    val magnesium: NutritionItem?,
    val phosphorus: NutritionItem?,
    val potassium: NutritionItem?,
    val zinc: NutritionItem?,
    val cholesterol: NutritionItem?
)

data class NutritionItem(
    val per100g: Double?,
    val perServing: Double?,
    val unit: String,
    val name: String
)

data class HalalInfo(
    val halalStatus: HalalStatus,
    val certificateNumber: String?,
    val certificationBody: String?,
    val validUntil: String?,
    val lastChecked: String?,
    val source: String?
)

enum class HalalStatus {
    HALAL,
    NON_HALAL,
    UNKNOWN;

    companion object {
        fun fromString(value: String): HalalStatus {
            return when (value.lowercase()) {
                "halal" -> HALAL
                "non_halal", "non-halal" -> NON_HALAL
                else -> UNKNOWN
            }
        }
    }
}

// Network Models
data class HalalCheckRequest(
    @SerializedName("barcode") val barcode: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("brand") val brand: String?
)

data class HalalCheckResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("halal_status") val halal_status: String,
    @SerializedName("certificate_number") val certificate_number: String?,
    @SerializedName("certification_body") val certification_body: String?,
    @SerializedName("valid_until") val valid_until: String?,
    @SerializedName("last_checked") val last_checked: String?,
    @SerializedName("source") val source: String?
)

data class HalalProductResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ProductDetailData
)

data class ProductDetailData(
    @SerializedName("product") val product: ProductInfo,
    @SerializedName("halal_info") val halal_info: HalalInfoResponse,
    @SerializedName("halal_source") val halal_source: String
)

data class ProductInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("image") val image: String?
)

data class HalalInfoResponse(
    @SerializedName("halal_status") val halal_status: String,
    @SerializedName("halal_certificate_number") val halal_certificate_number: String?,
    @SerializedName("certification_body") val certification_body: String?,
    @SerializedName("certificate_valid_until") val certificate_valid_until: String?,
    @SerializedName("last_checked_at") val last_checked_at: String?
)

data class UnifiedProductAlternativeResponse(
    @SerializedName("original_product") val original_product: String,
    @SerializedName("alternatives") val alternatives: List<UnifiedProductData>,
    @SerializedName("success") val success: Boolean = true
)

/** Dipakai oleh [ApiService.getProductAlternatives] & ProductRepository. */
data class HalalAlternativeResponse(
    @SerializedName("problematic_ingredients_reason") val problematic_ingredients_reason: String,
    @SerializedName("halal_alternatives") val halal_alternatives: List<HalalAlternativeItem>,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("success") val success: Boolean = true
)

data class HalalAlternativeItem(
    @SerializedName("name") val name: String,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("brand") val brand: String? = null,
    @SerializedName("reason_it_is_better") val reason_it_is_better: String
)

// Open Food Facts API Models
data class OpenFoodFactsResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("status_verbose") val status_verbose: String? = null,
    @SerializedName("product") val product: OpenFoodFactsProduct?,
    @SerializedName("warning") val warning: String? = null
)

data class OpenFoodFactsProduct(
    val id: String,
    val product_name: String?,
    val product_name_en: String?,
    val product_name_fr: String?,
    val generic_name: String?,
    val generic_name_en: String?,
    val generic_name_fr: String?,
    val quantity: String?,
    val serving_size: String?,
    val serving_size_unit: String?,
    val brands: String?,
    val brands_tags: List<String>?,
    val categories: String?,
    val categories_tags: List<String>?,
    val labels: String?,
    val labels_tags: List<String>?,
    val countries: String?,
    val countries_tags: List<String>?,
    val ingredients_text: String?,
    val ingredients_text_en: String?,
    val ingredients_text_fr: String?,
    val allergens: String?,
    val allergens_en: String?,
    val allergens_fr: String?,
    val traces: String?,
    val traces_en: String?,
    val traces_fr: String?,
    val additives: String?,
    val additives_en: String?,
    val additives_fr: String?,
    val nutriscore_score: Int?,
    val nutriscore_grade: String?,
    val nova_group: Int?,
    val nova_group_name: String?,
    val unique_scans_n: Int?,
    val last_modified_t: String?,
    val created_t: String?,
    val image_url: String?,
    val image_front_url: String?,
    val image_ingredients_url: String?,
    val image_nutrition_url: String?,
    val stores: String?,
    val packaging: String?,
    val ingredients_analysis_tags: List<String>?,
    val nutriments: Map<String, Double>?,
    val nutrient_levels: Map<String, String>?,
    val nutrition_data_per: String?
)

// Premium Feature Models
data class WeeklyStatsResponse(
    val response_code: Int,
    val message: String,
    val content: List<WeeklyStatItem>
)

data class WeeklyStatItem(
    val date: String,
    val status: String,
    val count: Int
)

data class RecommendationsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ProductInfo>? = null,
    @SerializedName("message") val message: String? = null
)




// AI Meal Analysis Models
data class AiAnalysisState(
    val isLoading: Boolean = false,
    val data: MealData? = null,
    val error: String? = null
)

data class MealAnalysisRequest(
    @SerializedName("image") val image: String
)

data class MealData(
    @SerializedName("meal_name") val mealName: String,
    @SerializedName("description") val description: String,
    @SerializedName("halal_analysis") val halalAnalysis: MealHalalAnalysis,
    @SerializedName("health_score") val healthScore: Int,
    @SerializedName("health_grade") val healthGrade: String,
    @SerializedName("nutrition") val nutrition: MealNutrition,
    @SerializedName("portion_advice") val portionAdvice: String
)

data class MealHalalAnalysis(
    @SerializedName("status") val status: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("risk_factors") val riskFactors: List<String>
)

data class MealNutrition(
    @SerializedName("calories") val calories: Int,
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fat") val fat: Double,
    @SerializedName("sugar") val sugar: Double
)


