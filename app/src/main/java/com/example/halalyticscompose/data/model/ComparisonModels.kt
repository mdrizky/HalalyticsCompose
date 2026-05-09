package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class ComparisonRequest(
    @SerializedName("barcodes") val barcodes: List<String>,
    @SerializedName("family_id") val familyId: Int? = null
)

data class ComparisonResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ComparisonData?,
    @SerializedName("products") val products: List<StandardizedProduct>?
)

data class ComparisonData(
    @SerializedName("comparison") val comparison: List<ProductComparison>,
    @SerializedName("better_choice") val betterChoice: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("similarities") val similarities: List<String>? = null
)

data class ProductComparison(
    @SerializedName("product_name") val productName: String,
    @SerializedName("halal_score") val halalScore: Int,
    @SerializedName("safety_score") val safetyScore: Int,
    @SerializedName("pros") val pros: List<String>,
    @SerializedName("cons") val cons: List<String>,
    @SerializedName("suitability_notes") val suitabilityNotes: String
)

data class StandardizedProduct(
    @SerializedName("barcode") val barcode: String,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("ingredients_text") val ingredientsText: String?,
    @SerializedName("status_halal") val statusHalal: String,
    @SerializedName("halal_certificate") val halalCertificate: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("nutriscore") val nutriscore: String?
)
