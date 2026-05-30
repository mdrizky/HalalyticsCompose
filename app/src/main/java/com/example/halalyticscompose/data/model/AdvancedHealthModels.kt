package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName


// ----------------- Nutrition Scan Models ----------------- //

data class NutritionScanRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("image_base64") val imageBase64: String
)

data class NutritionScanResponse(
    val success: Boolean,
    val data: NutritionScanData
)

data class NutritionScanData(
    val id: Int,
    @SerializedName("product_image_path") val imagePath: String,
    @SerializedName("ai_nutrition_analysis") val aiAnalysis: String,
    @SerializedName("halal_status") val halalStatus: String,
    @SerializedName("health_score") val healthScore: Int
)

// ----------------- Medical Records Models ----------------- //

data class MedicalRecordRequest(
    @SerializedName("id_user") val userId: Int,
    @SerializedName("record_type") val recordType: String,
    @SerializedName("record_date") val recordDate: String,
    val title: String,
    val description: String? = null,
    @SerializedName("hospital_name") val hospitalName: String? = null,
    @SerializedName("doctor_name") val doctorName: String? = null,
    @SerializedName("image_base64") val imageBase64: String? = null,
    val tags: List<String>? = emptyList()
)

data class MedicalRecordListResponse(
    val success: Boolean,
    val data: List<MedicalRecordData>
)

data class MedicalRecordResponse(
    val success: Boolean,
    val data: MedicalRecordData
)

data class MedicalRecordData(
    val id: Int,
    @SerializedName("record_type") val recordType: String,
    @SerializedName("record_date") val recordDate: String,
    val title: String,
    val description: String?,
    @SerializedName("hospital_name") val hospitalName: String?,
    @SerializedName("doctor_name") val doctorName: String?,
    @SerializedName("file_path") val filePath: String?,
    val tags: String?
)

// ----------------- Emergency Models ----------------- //

data class EmergencyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("emergency_type") val emergencyType: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class EmergencyResponse(
    val success: Boolean,
    val guidance: List<String>,
    val data: EmergencyLogData
)

data class EmergencyLogData(
    val id: Int,
    @SerializedName("emergency_type") val emergencyType: String,
    @SerializedName("ai_guidance") val aiGuidance: String
)

// ----------------- AI Insight & Health Score ----------------- //

// (Moved to HealthModels.kt to avoid redeclaration)

