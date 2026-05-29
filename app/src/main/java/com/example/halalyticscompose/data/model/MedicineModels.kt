package com.example.halalyticscompose.data.model

import com.google.gson.annotations.SerializedName

data class MedicineCheckResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("source") val source: String, // database vs ai_generated
    @SerializedName("data") val data: MedicineData?
)

data class MedicineData(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("id_medicine") val idMedicine: Int? = null,
    @SerializedName("name") val name: String,
    @SerializedName("generic_name") val genericName: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("indications") val indications: String? = null,
    @SerializedName("ingredients") val ingredients: List<String>? = null,
    @SerializedName("dosage_info") val dosageInfo: String? = null,
    @SerializedName("frequency_per_day") val frequencyPerDay: Int? = null,
    @SerializedName("max_daily_dose") val maxDailyDose: String? = null,
    @SerializedName("side_effects") val sideEffects: String? = null,
    @SerializedName("warnings") val warnings: String? = null,
    @SerializedName("contraindications") val contraindications: String? = null,
    @SerializedName("route") val route: String? = null,
    @SerializedName("halal_status") val halalStatus: String = "syubhat",
    @SerializedName("halal_certificate_number") val halalCertNumber: String? = null,
    @SerializedName("bpom_status") val bpomStatus: String? = "unverified",
    @SerializedName("bpom_number") val bpomNumber: String? = null,
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("country_origin") val countryOrigin: String? = null,
    @SerializedName("dosage_form") val dosageForm: String? = null,
    @SerializedName("category") val kategori: String? = null,
    @SerializedName("source") val source: String? = "local",
    @SerializedName("is_imported_from_fda") val isImportedFromFda: Boolean? = false,
    @SerializedName("external_reference") val externalReference: String? = null,
    @SerializedName("is_prescription_required") val isPrescriptionRequired: Boolean? = false,
    @SerializedName("is_verified_by_admin") val isVerifiedByAdmin: Boolean? = false,
    @SerializedName("active") val active: Boolean? = true
)

// Medication Reminder Item for Lists & Details
data class MedicationReminderItem(
    @SerializedName(value = "id", alternate = ["id_reminder"]) val id: Int = 0,
    @SerializedName(value = "user_id", alternate = ["id_user"]) val userId: Int = 0,
    @SerializedName(value = "drug_id", alternate = ["id_medicine"]) val drugId: Int = 0,
    @SerializedName("dosage") val dosage: String = "",
    @SerializedName("frequency") val frequency: String = "",
    @SerializedName(value = "time_slots", alternate = ["schedule_times"]) val timeSlots: List<String>? = null,
    @SerializedName("start_date") val startDate: String = "",
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("medicine_name") val medicineName: String = "",
    @SerializedName("symptoms") val symptoms: String? = null,
    @SerializedName("frequency_per_day") val frequencyPerDay: Int = 1,
    @SerializedName(value = "schedule_times", alternate = ["time_slots"]) val scheduleTimes: List<String>? = null,
    @SerializedName("taken_times") val takenTimes: List<String>? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("voice_message") val voiceMessage: VoiceMessages? = null,
    @SerializedName(value = "drug", alternate = ["medicine"]) val drug: MedicineData? = null
)

data class VoiceMessages(
    @SerializedName("formal") val formal: String?,
    @SerializedName("casual") val casual: String?,
    @SerializedName("motivational") val motivational: String?
)

// Drug Interaction Models
data class DrugInteractionData(
    @SerializedName("has_interaction") val hasInteraction: Boolean = false,
    @SerializedName("severity") val severity: String = "minor",
    @SerializedName("description") val description: String = "Belum ada data interaksi.",
    @SerializedName("recommendation") val recommendation: String? = null,
    @SerializedName("scientific_basis") val scientificBasis: String? = null,
    @SerializedName("sources") val sources: List<String>? = null,
    @SerializedName("disclaimer") val disclaimer: String? = null
)

// Pill Identification Models
data class PillIdentifyData(
    @SerializedName("possible_drugs") val possibleDrugs: List<PossibleDrug>,
    @SerializedName("visual_features") val visualFeatures: VisualFeatures
)

data class PossibleDrug(
    @SerializedName("name") val name: String,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("generic_name") val genericName: String?,
    @SerializedName("description") val description: String?
)

data class VisualFeatures(
    @SerializedName("shape") val shape: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("imprint") val imprint: String?
)

// Health Metrics Models
data class HealthMetricData(
    @SerializedName("id") val id: Int,
    @SerializedName("metric_type") val metricType: String,
    @SerializedName("value") val value: String,
    @SerializedName("recorded_at") val recordedAt: String,
    @SerializedName("notes") val notes: String?
)

// Halal Alternatives Models
data class HalalAlternativeData(
    @SerializedName("problematic_ingredients") val problematicIngredients: List<String>?,
    @SerializedName("halal_alternatives") val halalAlternatives: List<HalalAlternativeItem>?,
    @SerializedName("explanation") val explanation: String?
)

data class HalalAlternativeItem(
    @SerializedName("name") val name: String,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("halal_cert") val halalCert: String?,
    @SerializedName("confidence") val confidence: Double?
)

// Original/Legacy classes for backward compatibility
data class MedicineReminder(
    @SerializedName("id") val id: Int,
    @SerializedName("drug") val drug: DrugInfo?,
    @SerializedName("medicine_name") val medicineName: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("schedule_times") val scheduleTimes: List<String>?,
    @SerializedName("time_slots") val timeSlots: List<String>?
)

data class DrugInfo(
    @SerializedName("name") val name: String
)

data class MedicineScheduleRequest(
    @SerializedName("medicine_id") val medicineId: Int?,
    @SerializedName("custom_name") val customName: String?,
    @SerializedName("dosage") val dosage: String,
    @SerializedName("reminder_time") val reminderTime: String
)
