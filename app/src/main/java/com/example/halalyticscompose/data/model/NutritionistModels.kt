package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class NutritionistDashboardResponse(
    val success: Boolean,
    @SerializedName("medical_disclaimer") val medicalDisclaimer: String? = null,
    val message: String? = null,
    val data: NutritionistDashboardData? = null,
)

data class NutritionistDashboardData(
    @SerializedName("active_consultations") val activeConsultations: Int = 0,
    @SerializedName("total_patients_distinct") val totalPatientsDistinct: Int = 0,
    @SerializedName("population_bmi_snapshot") val populationBmiSnapshot: PopulationBmiSnapshot? = null,
    @SerializedName("recent_consultations") val recentConsultations: List<RecentConsultationSummary>? = null,
)

data class PopulationBmiSnapshot(
    @SerializedName("obesity_bmi_gte_30") val obesityBmiGte30: Int = 0,
    @SerializedName("underweight_bmi_lt_18_5") val underweightBmiLt185: Int = 0,
    @SerializedName("other_recorded_bmi") val otherRecordedBmi: Int = 0,
)

data class RecentConsultationSummary(
    val id: Long = 0,
    val subject: String? = null,
    @SerializedName("user_name") val userName: String? = null,
    @SerializedName("user_bmi") val userBmi: Double? = null,
)
