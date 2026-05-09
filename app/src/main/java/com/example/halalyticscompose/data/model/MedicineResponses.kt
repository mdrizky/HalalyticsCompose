package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

// Symptoms Analysis Response
data class SymptomsAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SymptomsAnalysisDataWrapper? = null,
    // Keep top-level fields in case backend sends it flat sometimes
    @SerializedName("symptoms_analysis") val flat_symptoms_analysis: SymptomsAnalysis? = null,
    @SerializedName("recommended_medicines") val flat_recommended_medicines: List<com.example.halalyticscompose.data.model.MedicineData>? = null
) {
    val symptoms_analysis: SymptomsAnalysis? get() = data?.symptoms_analysis ?: flat_symptoms_analysis
    val recommended_medicines: List<com.example.halalyticscompose.data.model.MedicineData>? get() = data?.recommended_medicines ?: flat_recommended_medicines
}

data class SymptomsAnalysisDataWrapper(
    @SerializedName("symptoms_analysis") val symptoms_analysis: SymptomsAnalysis? = null,
    @SerializedName("recommended_medicines") val recommended_medicines: List<com.example.halalyticscompose.data.model.MedicineData>? = null
)

data class SymptomsAnalysis(
    // Profile confirmation — AI confirms it read the profile
    @SerializedName("profil_pasien_dibaca") val profil_pasien_dibaca: String? = null,
    @SerializedName("catatan_lokasi") val catatan_lokasi: String? = null,
    
    @SerializedName("ringkasan_keluhan") val ringkasan_keluhan: String? = null,
    @SerializedName("condition") val condition: String = "Unknown Condition",
    @SerializedName(value = "severity_label", alternate = ["tingkat_keparahan_label"]) val severity_label: String? = null,
    @SerializedName("why_it_happened") val why_it_happened: String? = null,
    @SerializedName("gejala_terkait") val gejala_terkait: List<String> = emptyList(),
    @SerializedName("possible_causes") val possible_causes: List<String> = emptyList(),
    @SerializedName("possible_causes_detailed") val possible_causes_detailed: List<PossibleCauseDetail> = emptyList(),
    @SerializedName("severity") val severity: String = "mild",
    @SerializedName("alasan_keparahan") val alasan_keparahan: String? = null,
    @SerializedName("emergency_warning") val emergency_warning: String? = null,
    @SerializedName("triage_action") val triage_action: String? = null,
    @SerializedName("doctor_recommendation") val doctor_recommendation: String? = null,
    @SerializedName("should_seek_doctor") val should_seek_doctor: Boolean = false,
    
    // Core Medical Advice
    @SerializedName("recommendation") val recommendation: String = "Konsultasikan dengan dokter",
    @SerializedName("future_prevention") val future_prevention: String? = null,
    @SerializedName("lifestyle_advice") val lifestyle_advice: String? = null,
    @SerializedName("disease_explanations") val disease_explanations: List<DiseaseExplanation> = emptyList(),
    @SerializedName("trigger_factors") val trigger_factors: List<String> = emptyList(),
    
    // Herbal Remedies — displayed BEFORE pharmacy medicines
    @SerializedName("herbal_remedies") val herbal_remedies: List<HerbalRemedy> = emptyList(),
    
    // Ingredients & Medicines
    @SerializedName(value = "recommended_ingredients", alternate = ["active_ingredients"]) 
    val recommended_ingredients: List<String> = emptyList(),
    @SerializedName("medicine_categories") val medicine_categories: List<String> = emptyList(),
    @SerializedName("recommended_medicines_list") val recommended_medicines_list: List<String> = emptyList(),
    @SerializedName("recommended_medicine_details") val recommended_medicine_details: List<RecommendedMedicineDetail> = emptyList(),
    @SerializedName("alternative_medicines") val alternative_medicines: List<String> = emptyList(),
    
    // Usage Details
    @SerializedName("usage_instructions") val usage_instructions: String? = null,
    @SerializedName("dosage_guidelines") val dosage_guidelines: String? = null,
    @SerializedName("when_to_take_and_frequency") val when_to_take_and_frequency: String? = null,
    @SerializedName("side_effects") val side_effects: List<String> = emptyList(),
    @SerializedName("drug_mechanism") val drug_mechanism: String? = null,
    @SerializedName("first_aid_steps") val first_aid_steps: List<String> = emptyList(),
    @SerializedName("prevention") val prevention: List<String> = emptyList(),
    @SerializedName("follow_up_questions") val follow_up_questions: List<String> = emptyList(),
    @SerializedName("confidence_level") val confidence_level: String? = null,
    @SerializedName("tldr") val tldr: String? = null,
    
    @SerializedName("halal_check") val halal_check: HalalCheck? = null
) {
    // Backward compatibility
    val active_ingredients: List<String> get() = recommended_ingredients
}

data class HerbalRemedy(
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("how_to_prepare") val how_to_prepare: String? = null,
    @SerializedName("how_to_use") val how_to_use: String? = null,
    @SerializedName("frequency") val frequency: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("precautions") val precautions: String? = null
)

data class HalalCheck(
    @SerializedName("status") val status: String = "unknown",
    @SerializedName("notes") val notes: String = "Belum dianalisis"
)

data class PossibleCauseDetail(
    @SerializedName("name") val name: String = "",
    @SerializedName("percentage") val percentage: Int? = null,
    @SerializedName("reason") val reason: String? = null
)

data class DiseaseExplanation(
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("relation_to_case") val relation_to_case: String? = null
)

data class RecommendedMedicineDetail(
    @SerializedName("name") val name: String = "",
    @SerializedName("function") val function: String? = null,
    @SerializedName("dosage") val dosage: String? = null,
    @SerializedName("how_to_take") val how_to_take: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("when_to_take") val when_to_take: String? = null,
    @SerializedName("halal_status") val halal_status: String? = null,
    @SerializedName("price_range") val price_range: String? = null,
    @SerializedName("safety_note") val safety_note: String? = null,
    @SerializedName("side_effects") val side_effects: List<String> = emptyList()
)

// Medicine Responses
data class MedicineSearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("source") val source: String? = null,
    @SerializedName("data") val data: List<com.example.halalyticscompose.data.model.MedicineData>? = null,
    @SerializedName("message") val message: String? = null
)

// Medication Reminder Response Wrapper
data class MedicationReminderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("reminder") val reminder: com.example.halalyticscompose.data.model.MedicationReminderItem? = null,
    @SerializedName("data") val data: Any? = null // For variants that use "data" field
)

// User Reminders Response Wrapper
data class UserMedicineRemindersResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<com.example.halalyticscompose.data.model.MedicationReminderItem>? = null,
    @SerializedName("message") val message: String? = null
)

typealias UserRemindersResponse = UserMedicineRemindersResponse

// Next Dose Response
data class NextDoseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("next_doses") val next_doses: List<NextDose>? = null,
    @SerializedName("message") val message: String? = null
)

data class NextDose(
    @SerializedName("reminder_id") val reminder_id: Int,
    @SerializedName("medicine_name") val medicine_name: String,
    @SerializedName("next_dose_time") val next_dose_time: String,
    @SerializedName("dose_info") val dose_info: String? = null
)

// AI Health Suite Responses
data class DrugInteractionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("source") val source: String?,
    @SerializedName("data") val data: com.example.halalyticscompose.data.model.DrugInteractionData
)

data class PillIdentifyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: com.example.halalyticscompose.data.model.PillIdentifyData,
    @SerializedName("image_url") val imageUrl: String?
)


data class HealthMetricResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<com.example.halalyticscompose.data.model.HealthMetricData>
)

data class HalalAlternativeResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("source") val source: String?,
    @SerializedName("data") val data: com.example.halalyticscompose.data.model.HalalAlternativeData
)

// Safe Schedule Response
data class SafeScheduleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SafeScheduleData? = null
)

data class SafeScheduleData(
    @SerializedName("medicine") val medicine: SafeScheduleMedicine? = null,
    @SerializedName("dosage") val dosage: String? = null,
    @SerializedName("frequency_per_day") val frequencyPerDay: Int? = null,
    @SerializedName("meal_relation") val mealRelation: String? = null,
    @SerializedName("meal_instruction") val mealInstruction: String? = null,
    @SerializedName("schedule_times") val scheduleTimes: List<String> = emptyList(),
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("duration_days") val durationDays: Int? = null,
    @SerializedName("disclaimer") val disclaimer: String? = null
)

data class SafeScheduleMedicine(
    @SerializedName("id_medicine") val idMedicine: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("generic_name") val genericName: String? = null,
    @SerializedName("source") val source: String? = null
)

data class PersonalRiskScoreResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("date") val date: String? = null,
    @SerializedName("risk_score") val riskScore: Int? = null,
    @SerializedName("risk_level") val riskLevel: String? = null,
    @SerializedName("alerts") val alerts: List<String> = emptyList(),
    @SerializedName("recommendation") val recommendation: String? = null,
    @SerializedName("disclaimer") val disclaimer: String? = null,
    @SerializedName("totals") val totals: RiskTotals? = null,
    @SerializedName("limits") val limits: RiskLimits? = null
)

data class RiskTotals(
    @SerializedName("sugar_g") val sugarG: Double = 0.0,
    @SerializedName("sodium_mg") val sodiumMg: Double = 0.0,
    @SerializedName("fat_g") val fatG: Double = 0.0,
    @SerializedName("calories") val calories: Int = 0
)

data class RiskLimits(
    @SerializedName("sugar_g") val sugarG: Double = 50.0,
    @SerializedName("sodium_mg") val sodiumMg: Double = 2300.0,
    @SerializedName("fat_g") val fatG: Double = 67.0,
    @SerializedName("calories") val calories: Double = 2000.0
)

data class DrugFoodConflictResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: DrugFoodConflictData? = null,
    @SerializedName("message") val message: String? = null
)

data class DrugFoodConflictData(
    @SerializedName("medicine_name") val medicineName: String? = null,
    @SerializedName("has_conflict") val hasConflict: Boolean = false,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("lookback_minutes") val lookbackMinutes: Int = 180,
    @SerializedName("matches") val matches: List<DrugFoodConflictMatch> = emptyList(),
    @SerializedName("recommendation") val recommendation: String? = null,
    @SerializedName("disclaimer") val disclaimer: String? = null
)

data class DrugFoodConflictMatch(
    @SerializedName("food_name") val foodName: String? = null,
    @SerializedName("matched_keyword") val matchedKeyword: String? = null,
    @SerializedName("severity") val severity: String? = null,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("time") val time: String? = null
)
