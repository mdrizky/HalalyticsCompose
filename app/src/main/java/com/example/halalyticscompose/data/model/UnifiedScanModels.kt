package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class UnifiedScanResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("source") val source: String,
    @SerializedName("data") val data: UnifiedProductData?,
    @SerializedName("analysis") val analysis: AiAnalysisData?,
    @SerializedName("message") val message: String,
    @SerializedName("needs_verification") val needsVerification: Boolean = false,
    @SerializedName("halal_issues") val halalIssues: List<String> = emptyList(),
    @SerializedName("action") val action: String? = null,
    @SerializedName("instructions") val instructions: List<String> = emptyList(),
    @SerializedName("family_box") val familyBox: List<FamilyScanResult>? = null,
    @SerializedName("crowd_status") val crowdStatus: CrowdStatus? = null
)

data class UnifiedProductData(
    @SerializedName("id") val id: Int,
    @SerializedName("nama_product") val namaProduct: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("image") val image: String?,
    @SerializedName("halal_status") val halalStatus: String,
    @SerializedName("verification_status") val verificationStatus: String,
    @SerializedName("source") val source: String,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("komposisi") val komposisi: String?,
    @SerializedName("info_gizi") val infoGizi: Map<String, Any>?,
    @SerializedName("kategori") val kategori: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("quantity") val quantity: String?,
    @SerializedName("packaging") val packaging: String?,
    @SerializedName("labels") val labels: String?,
    @SerializedName("stores") val stores: String?,
    @SerializedName("countries") val countries: String?,
    @SerializedName("nutriscore") val nutriscore: String?,
    @SerializedName("nova_group") val novaGroup: Int?,
    @SerializedName("ai_summary") val aiSummary: String?
)

data class FamilyScanResult(
    @SerializedName("member_id") val memberId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("relationship") val relationship: String?,
    @SerializedName("is_safe") val isSafe: Boolean,
    @SerializedName("risk_level") val riskLevel: String,
    @SerializedName("warnings") val warnings: List<String>
)

data class CrowdStatus(
    @SerializedName("total_reports") val totalReports: Int,
    @SerializedName("fake_reports") val fakeReports: Int,
    @SerializedName("status") val status: String
)

data class AiAnalysisData(
    @SerializedName("status_halal") val statusHalal: String,
    @SerializedName("health_score") val healthScore: Int,
    @SerializedName("health_status") val healthStatus: String?,
    @SerializedName("personalized_message") val personalizedMessage: String?,
    @SerializedName("watchouts") val watchouts: List<String>,
    @SerializedName("recommendations") val recommendations: List<String>?
)
