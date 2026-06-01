package com.example.halalyticscompose.data.model

import androidx.compose.ui.graphics.Color
import com.example.halalyticscompose.ui.theme.*
import com.google.gson.annotations.SerializedName

/**
 * Base Response dari Laravel
 */
data class BaseResponse<T>(
    @SerializedName("response_code")
    val responseCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("content")
    val content: T?
)

/**
 * Response untuk search products
 */
data class ExternalSearchResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("count")
    val count: Int,
    
    @SerializedName("page")
    val page: Int?,
    
    @SerializedName("page_size")
    val pageSize: Int?,
    
    @SerializedName("filter")
    val filter: String?,
    
    @SerializedName("products")
    val products: List<ProductItem>
)

/**
 * Product Item dari OpenFoodFacts
 */
data class ProductItem(
    @SerializedName("source")
    val source: String? = null,

    @SerializedName("barcode")
    val barcode: String? = null,

    @SerializedName("_id")
    val id: String?,
    
    @SerializedName("code")
    val code: String?,
    
    @SerializedName("product_name")
    val productName: String?,
    
    @SerializedName("product_name_en")
    val productNameEn: String?,
    
    @SerializedName("brands")
    val brands: String?,
    
    @SerializedName("brands_tags")
    val brandsTags: List<String>?,
    
    @SerializedName("quantity")
    val quantity: String?,
    
    @SerializedName("categories")
    val categories: String?,
    
    @SerializedName("categories_tags")
    val categoriesTags: List<String>?,
    
    @SerializedName("countries")
    val countries: String?,
    
    @SerializedName("countries_tags")
    val countriesTags: List<String>?,
    
    @SerializedName("image_url")
    val imageUrl: String?,
    
    @SerializedName("image_front_url")
    val imageFrontUrl: String?,
    
    @SerializedName("image_front_small_url")
    val imageFrontSmallUrl: String?,
    
    @SerializedName("image_front_thumb_url")
    val imageFrontThumbUrl: String?,
    
    @SerializedName("nutriscore_grade")
    val nutriscoreGrade: String?,
    
    @SerializedName("nutriscore_score")
    val nutriscoreScore: Any?,
    
    @SerializedName("nova_group")
    val novaGroup: Any?,
    
    @SerializedName("ingredients_text")
    val ingredientsText: String?,
    
    @SerializedName("ingredients_text_en")
    val ingredientsTextEn: String?,
    
    @SerializedName("allergens")
    val allergens: String?,
    
    @SerializedName("allergens_tags")
    val allergensTags: List<String>?,
    
    @SerializedName("labels")
    val labels: String?,
    
    @SerializedName("labels_tags")
    val labelsTags: List<String>?,
    
    @SerializedName("manufacturing_places")
    val manufacturingPlaces: String?,
    
    @SerializedName("origin")
    val origin: String?,
    
    @SerializedName("packaging")
    val packaging: String?,
    
    @SerializedName("stores")
    val stores: String?,

    @SerializedName("nutriments")
    val nutriments: Map<String, Any?>? = null,

    @SerializedName("synced_at")
    val syncedAt: String? = null,
    
    @SerializedName("halal_analysis")
    val halalAnalysis: HalalAnalysis?
) {
    /**
     * Get best available image
     */
    fun getBestImageUrl(): String? {
        return imageFrontSmallUrl 
            ?: imageFrontThumbUrl 
            ?: imageFrontUrl 
            ?: imageUrl
    }
    
    /**
     * Get display name (prioritize non-null)
     */
    fun getDisplayName(): String {
        return productName?.takeIf { it.isNotBlank() }
            ?: productNameEn?.takeIf { it.isNotBlank() }
            ?: "Unknown Product"
    }

    fun getNutrimentNumber(vararg keys: String): String? {
        val map = nutriments ?: return null
        for (key in keys) {
            val value = map[key] ?: continue
            when (value) {
                is Number -> return if (value.toDouble() % 1.0 == 0.0) value.toInt().toString() else String.format("%.2f", value.toDouble())
                is String -> if (value.isNotBlank()) return value
            }
        }
        return null
    }
    
    /**
     * Check if product has halal label
     */
    fun isHalal(): Boolean {
        val halalLabels = listOf("halal", "en:halal")
        return labelsTags?.any { tag -> 
            halalLabels.any { halal -> tag.contains(halal, ignoreCase = true) }
        } ?: false
    }
    
    /**
     * Check if vegetarian
     */
    fun isVegetarian(): Boolean {
        val vegLabels = listOf("vegetarian", "en:vegetarian")
        return labelsTags?.any { tag -> 
            vegLabels.any { veg -> tag.contains(veg, ignoreCase = true) }
        } ?: false
    }
    
    /**
     * Check if vegan
     */
    fun isVegan(): Boolean {
        val veganLabels = listOf("vegan", "en:vegan")
        return labelsTags?.any { tag -> 
            veganLabels.any { vegan -> tag.contains(vegan, ignoreCase = true) }
        } ?: false
    }
    
    /**
     * Get unified halal status - used across search and detail screens
     */
    fun getHalalStatus(): String {
        return when {
            isHalal() -> "Halal"
            halalAnalysis?.isPotentiallyHalal == true -> "Likely Halal"
            halalAnalysis?.isPotentiallyHalal == false -> "Check Required"
            else -> "Unknown"
        }
    }
    
    /**
     * Get halal status color
     */
    fun getHalalStatusColor(): Color {
        return when {
            isHalal() -> HalalColor
            halalAnalysis?.isPotentiallyHalal == true -> HalalGreen
            halalAnalysis?.isPotentiallyHalal == false -> HaramColor
            else -> TextMuted
        }
    }

}

/**
 * Halal Analysis dari backend
 */
data class HalalAnalysis(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("is_potentially_halal")
    val isPotentiallyHalal: Boolean,
    
    @SerializedName("suspicious_ingredients")
    val suspiciousIngredients: List<String>,
    
    @SerializedName("recommendation")
    val recommendation: String
)
