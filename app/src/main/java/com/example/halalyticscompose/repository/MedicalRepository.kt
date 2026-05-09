package com.example.halalyticscompose.repository

import android.util.Log
import com.example.halalyticscompose.BuildConfig
import com.example.halalyticscompose.data.model.SymptomsAnalysis
import com.example.halalyticscompose.data.model.HalalCheck
import com.example.halalyticscompose.utils.MegaPromptBuilder
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MedicalRepository — Direct AI fallback for symptom analysis.
 *
 * This repository is used when the Laravel backend (apiService.analyzeSymptoms)
 * fails or is unreachable. It calls the AI API directly from the client.
 *
 * Priority: Gemini (key already configured) → Anthropic (if key provided)
 */
@Singleton
class MedicalRepository @Inject constructor() {

    companion object {
        private const val TAG = "MedicalRepo"

        // Gemini endpoint
        private const val GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

        // Anthropic endpoint
        private const val ANTHROPIC_URL =
            "https://api.anthropic.com/v1/messages"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /**
     * Analyze symptoms using direct AI call.
     * Tries Gemini first (key already configured), then Anthropic if available.
     */
    suspend fun analyzeSymptomsDirect(
        symptoms: String,
        age: Int? = null,
        weight: Float? = null,
        height: Float? = null,
        gender: String? = null,
        allergies: String? = null,
        medicalHistory: String? = null,
        isGlutenFree: Boolean = false,
        hasNutAllergy: Boolean = false,
        bloodType: String? = null,
        activityLevel: String? = null,
        dietPreference: String? = null,
        address: String? = null,
        city: String? = null,
        province: String? = null
    ): SymptomsAnalysis {
        return withContext(Dispatchers.IO) {
            val geminiKey = BuildConfig.GEMINI_API_KEY
            val anthropicKey = BuildConfig.ANTHROPIC_API_KEY

            val profileContext = mapOf(
                "age" to age,
                "weight" to weight,
                "height" to height,
                "gender" to gender,
                "allergies" to allergies,
                "medicalHistory" to medicalHistory,
                "isGlutenFree" to isGlutenFree,
                "hasNutAllergy" to hasNutAllergy,
                "bloodType" to bloodType,
                "activityLevel" to activityLevel,
                "dietPreference" to dietPreference,
                "address" to address,
                "city" to city,
                "province" to province
            )

            // Try Gemini first (key is already configured in the project)
            if (geminiKey.isNotBlank()) {
                try {
                    Log.d(TAG, "Attempting Gemini analysis for: ${symptoms.take(50)}...")
                    return@withContext analyzeWithGemini(symptoms, geminiKey, profileContext)
                } catch (e: Exception) {
                    Log.e(TAG, "Gemini failed: ${e.message}", e)
                    // Fall through to Anthropic
                }
            }

            // Fallback to Anthropic
            if (anthropicKey.isNotBlank()) {
                try {
                    Log.d(TAG, "Attempting Anthropic analysis for: ${symptoms.take(50)}...")
                    return@withContext analyzeWithAnthropic(symptoms, anthropicKey, profileContext)
                } catch (e: Exception) {
                    Log.e(TAG, "Anthropic failed: ${e.message}", e)
                    throw e
                }
            }

            throw IllegalStateException("No AI API key configured. Set GEMINI_API_KEY or ANTHROPIC_API_KEY in local.properties")
        }
    }

    // ─── Gemini Implementation ─────────────────────────────────────

    private fun analyzeWithGemini(
        symptoms: String,
        apiKey: String,
        profile: Map<String, Any?>
    ): SymptomsAnalysis {
        val systemPrompt = MegaPromptBuilder.buildSystemPrompt()
        val userMessage = MegaPromptBuilder.buildPersonalizedUserMessage(
            symptoms = symptoms,
            age = profile["age"] as? Int,
            weight = profile["weight"] as? Float,
            height = profile["height"] as? Float,
            gender = profile["gender"] as? String,
            allergies = profile["allergies"] as? String,
            medicalHistory = profile["medicalHistory"] as? String,
            isGlutenFree = profile["isGlutenFree"] as? Boolean ?: false,
            hasNutAllergy = profile["hasNutAllergy"] as? Boolean ?: false,
            bloodType = profile["bloodType"] as? String,
            activityLevel = profile["activityLevel"] as? String,
            dietPreference = profile["dietPreference"] as? String,
            address = profile["address"] as? String,
            city = profile["city"] as? String,
            province = profile["province"] as? String
        )

        val requestBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 4096)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("$GEMINI_URL?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty Gemini response")

        if (!response.isSuccessful) {
            Log.e(TAG, "Gemini error ${response.code}: $body")
            throw Exception("Gemini API Error: ${response.code}")
        }

        val jsonResponse = JSONObject(body)
        val textContent = jsonResponse
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        return parseToSymptomsAnalysis(textContent)
    }

    // ─── Anthropic Implementation ──────────────────────────────────

    private fun analyzeWithAnthropic(
        symptoms: String,
        apiKey: String,
        profile: Map<String, Any?>
    ): SymptomsAnalysis {
        val systemPrompt = MegaPromptBuilder.buildSystemPrompt()
        val userMessage = MegaPromptBuilder.buildPersonalizedUserMessage(
            symptoms = symptoms,
            age = profile["age"] as? Int,
            weight = profile["weight"] as? Float,
            height = profile["height"] as? Float,
            gender = profile["gender"] as? String,
            allergies = profile["allergies"] as? String,
            medicalHistory = profile["medicalHistory"] as? String,
            isGlutenFree = profile["isGlutenFree"] as? Boolean ?: false,
            hasNutAllergy = profile["hasNutAllergy"] as? Boolean ?: false,
            bloodType = profile["bloodType"] as? String,
            activityLevel = profile["activityLevel"] as? String,
            dietPreference = profile["dietPreference"] as? String,
            address = profile["address"] as? String,
            city = profile["city"] as? String,
            province = profile["province"] as? String
        )

        val requestBody = JSONObject().apply {
            put("model", "claude-3-5-haiku-20241022")
            put("max_tokens", 2048)
            put("system", systemPrompt)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            })
        }

        val request = Request.Builder()
            .url(ANTHROPIC_URL)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty Anthropic response")

        if (!response.isSuccessful) {
            Log.e(TAG, "Anthropic error ${response.code}: $body")
            throw Exception("Anthropic API Error: ${response.code}")
        }

        val jsonResponse = JSONObject(body)
        val textContent = jsonResponse
            .getJSONArray("content")
            .getJSONObject(0)
            .getString("text")

        return parseToSymptomsAnalysis(textContent)
    }

    // ─── JSON Parsing ──────────────────────────────────────────────

    /**
     * Parses AI text response (JSON) into SymptomsAnalysis.
     * Maps the MegaPrompt JSON schema to the existing data model.
     */
    private fun parseToSymptomsAnalysis(jsonText: String): SymptomsAnalysis {
        return try {
            // Clean markdown code blocks if present
            val cleanJson = jsonText
                .replace(Regex("```json\\s*"), "")
                .replace(Regex("```\\s*$"), "")
                .trim()

            Log.d(TAG, "Parsing AI JSON: ${cleanJson.take(200)}...")

            val json = JSONObject(cleanJson)

            val parsedSeverity = mapSeverity(
                json.optString("severity").ifBlank { json.optString("severity_level", "LOW") }
            )
            val medicineDetails = parseRecommendedMedicineDetails(json)

            SymptomsAnalysis(
                profil_pasien_dibaca = json.optString("profil_pasien_dibaca").ifBlank { null },
                catatan_lokasi = json.optString("catatan_lokasi").ifBlank { null },
                ringkasan_keluhan = json.optString("ringkasan_keluhan").ifBlank {
                    json.optString("summary").ifBlank {
                        json.optString("description", "")
                    }
                },
                condition = json.optString("condition").ifBlank {
                    json.optString("diagnosis", "Perlu Evaluasi")
                },
                severity_label = json.optString("tingkat_keparahan_label").ifBlank {
                    severityLabelFrom(parsedSeverity)
                },
                severity = parsedSeverity,
                why_it_happened = buildWhyItHappened(json),
                gejala_terkait = jsonArrayToList(json.optJSONArray("gejala_terkait")),
                possible_causes = parsePossibleCauseStrings(json),
                possible_causes_detailed = parsePossibleCauseDetails(json),
                alasan_keparahan = json.optString("alasan_keparahan").ifBlank {
                    json.optString("monitoring", "")
                },
                emergency_warning = json.optString("emergency_warning").ifBlank {
                    if (parsedSeverity == "emergency") "Segera ke IGD atau hubungi tenaga medis." else null
                },
                triage_action = json.optString("triage_action").ifBlank {
                    json.optString("monitoring", "")
                },
                doctor_recommendation = json.optString("doctor_recommendation").ifBlank {
                    if (json.optBoolean("shouldSeeDoctor") || parsedSeverity == "emergency") {
                        "Segera konsultasikan ke dokter profesional."
                    } else null
                },
                should_seek_doctor = json.optBoolean("should_seek_doctor", json.optBoolean("shouldSeeDoctor", parsedSeverity == "emergency")),
                recommendation = json.optString("recommendation").ifBlank {
                    json.optString("monitoring", "Pantau kondisi Anda")
                },
                future_prevention = null,
                lifestyle_advice = json.optString("lifestyle_advice").ifBlank {
                    buildLifestyleAdvice(json)
                },
                disease_explanations = parseDiseaseExplanations(json),
                trigger_factors = jsonArrayToList(json.optJSONArray("trigger_factors")),
                recommended_ingredients = jsonArrayToList(json.optJSONArray("recommended_ingredients")),
                medicine_categories = emptyList(),
                recommended_medicines_list = medicineDetails.map { detail ->
                    listOf(detail.name, detail.dosage, detail.when_to_take)
                        .filterNot { it.isNullOrBlank() }
                        .joinToString(" - ")
                }.ifEmpty { buildMedicineNameList(json) },
                recommended_medicine_details = medicineDetails,
                alternative_medicines = jsonArrayToList(json.optJSONArray("alternative_medicines"))
                    .ifEmpty { jsonArrayToList(json.optJSONArray("jalur_alternatif")) },
                herbal_remedies = parseHerbalRemedies(json),
                usage_instructions = json.optString("usage_instructions").ifBlank {
                    buildUsageInstructions(json)
                },
                dosage_guidelines = json.optString("dosage_guidelines").ifBlank {
                    buildDosageGuidelines(json)
                },
                when_to_take_and_frequency = buildFrequencyInfo(json),
                side_effects = parseGlobalSideEffects(json, medicineDetails),
                drug_mechanism = json.optString("drug_mechanism").ifBlank { null },
                first_aid_steps = jsonArrayToList(json.optJSONArray("first_aid_steps")),
                prevention = jsonArrayToList(json.optJSONArray("prevention")),
                follow_up_questions = jsonArrayToList(json.optJSONArray("follow_up_questions")),
                confidence_level = json.optString("confidence_level").ifBlank { "Sedang" },
                tldr = json.optString("tldr").ifBlank { null },
                halal_check = parseHalalCheck(json, medicineDetails)
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error: ${e.message}", e)
            // Return a helpful fallback instead of crashing
            SymptomsAnalysis(
                ringkasan_keluhan = "Keluhan berhasil diterima tetapi format analisis AI sedang tidak stabil.",
                condition = "Analisis Tersedia Sebagian",
                severity = "mild",
                severity_label = "Ringan",
                why_it_happened = "AI berhasil merespons tetapi format data tidak sesuai. Silakan coba lagi.",
                possible_causes = listOf("Gangguan format respons AI"),
                recommendation = "Coba ulangi analisis atau konsultasikan ke dokter",
                tldr = "Analisis sementara tersedia sebagian. Jika gejala memburuk, segera konsultasi ke dokter.",
                should_seek_doctor = false
            )
        }
    }

    // ─── Helper Methods ────────────────────────────────────────────

    private fun mapSeverity(raw: String): String = when (raw.uppercase()) {
        "LOW" -> "mild"
        "MEDIUM" -> "moderate"
        "HIGH" -> "severe"
        "EMERGENCY" -> "emergency"
        else -> raw.lowercase()
    }

    private fun severityLabelFrom(severity: String): String = when (severity.lowercase()) {
        "mild" -> "Ringan"
        "moderate" -> "Sedang"
        "emergency", "severe", "high" -> "Perlu perhatian medis"
        else -> "Sedang"
    }

    private fun jsonArrayToList(array: org.json.JSONArray?): List<String> {
        if (array == null) return emptyList()
        return (0 until array.length()).map { array.getString(it) }
    }

    private fun buildLifestyleAdvice(json: JSONObject): String? {
        val recommendations = json.optJSONArray("recommendations") ?: return null
        return (0 until recommendations.length())
            .map { recommendations.getString(it) }
            .joinToString("\n• ", prefix = "• ")
    }

    private fun buildMedicineNameList(json: JSONObject): List<String> {
        val medicines = json.optJSONArray("medicines") ?: return emptyList()
        return (0 until medicines.length()).map { i ->
            val med = medicines.getJSONObject(i)
            val halal = if (med.optBoolean("isHalal", true)) "✓ Halal" else "⚠ Perlu verifikasi"
            "${med.optString("name")} ($halal)"
        }
    }

    private fun parseRecommendedMedicineDetails(json: JSONObject): List<com.example.halalyticscompose.data.model.RecommendedMedicineDetail> {
        val source = json.optJSONArray("recommended_medicines_list")
            ?: json.optJSONArray("medicines")
            ?: return emptyList()

        return (0 until source.length()).mapNotNull { index ->
            val item = source.optJSONObject(index) ?: return@mapNotNull null
            com.example.halalyticscompose.data.model.RecommendedMedicineDetail(
                name = item.optString("name").ifBlank { "Obat tidak disebutkan" },
                function = item.optString("function").ifBlank {
                    item.optString("notes").ifBlank { null }
                },
                dosage = item.optString("dosage").ifBlank {
                    item.optString("dose").ifBlank { null }
                },
                how_to_take = item.optString("how_to_take").ifBlank { null },
                duration = item.optString("duration").ifBlank { null },
                when_to_take = item.optString("when_to_take").ifBlank {
                    item.optString("frequency").ifBlank { null }
                },
                halal_status = item.optString("halal_status").ifBlank {
                    if (item.has("isHalal")) {
                        if (item.optBoolean("isHalal", true)) "Halal" else "Perlu cek label"
                    } else null
                },
                price_range = item.optString("price_range").ifBlank { 
                    item.optString("estimasi_harga").ifBlank { null }
                },
                safety_note = item.optString("safety_note").ifBlank {
                    item.optString("notes").ifBlank { null }
                },
                side_effects = jsonArrayToList(item.optJSONArray("side_effects"))
            )
        }
    }

    private fun parsePossibleCauseStrings(json: JSONObject): List<String> {
        val detailed = json.optJSONArray("possible_causes")
        if (detailed != null && detailed.length() > 0 && detailed.opt(0) is JSONObject) {
            return (0 until detailed.length()).mapNotNull { index ->
                detailed.optJSONObject(index)?.let { item ->
                    val name = item.optString("name").ifBlank { null } ?: return@let null
                    val percentage = item.optInt("percentage", -1)
                    if (percentage >= 0) "$name - $percentage%" else name
                }
            }
        }

        return jsonArrayToList(json.optJSONArray("potentialCauses"))
            .ifEmpty { jsonArrayToList(json.optJSONArray("possible_causes")) }
    }

    private fun parsePossibleCauseDetails(json: JSONObject): List<com.example.halalyticscompose.data.model.PossibleCauseDetail> {
        val array = json.optJSONArray("possible_causes") ?: return emptyList()
        if (array.length() == 0 || array.opt(0) !is JSONObject) return emptyList()

        return (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            com.example.halalyticscompose.data.model.PossibleCauseDetail(
                name = item.optString("name"),
                percentage = item.optInt("percentage").takeIf { it > 0 },
                reason = item.optString("reason").ifBlank { null }
            )
        }
    }

    private fun parseDiseaseExplanations(json: JSONObject): List<com.example.halalyticscompose.data.model.DiseaseExplanation> {
        val array = json.optJSONArray("disease_explanations") ?: return emptyList()
        return (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            com.example.halalyticscompose.data.model.DiseaseExplanation(
                name = item.optString("name"),
                description = item.optString("description").ifBlank { null },
                relation_to_case = item.optString("relation_to_case").ifBlank { null }
            )
        }
    }

    private fun buildWhyItHappened(json: JSONObject): String? {
        val direct = json.optString("why_it_happened").ifBlank { null }
        if (!direct.isNullOrBlank()) return direct

        val summary = json.optString("ringkasan_keluhan").ifBlank { null }
        val reason = json.optString("alasan_keparahan").ifBlank { null }
        val diseaseExplanation = parseDiseaseExplanations(json).firstOrNull()?.description

        return listOfNotNull(summary, reason, diseaseExplanation)
            .joinToString(" ")
            .ifBlank { json.optString("description").ifBlank { null } }
    }

    private fun parseHalalCheck(
        json: JSONObject,
        medicineDetails: List<com.example.halalyticscompose.data.model.RecommendedMedicineDetail>
    ): HalalCheck {
        val halalObject = json.optJSONObject("halal_check")
        if (halalObject != null) {
            return HalalCheck(
                status = halalObject.optString("status", "unknown"),
                notes = halalObject.optString("notes", "Belum dianalisis")
            )
        }

        val detailStatus = medicineDetails.firstOrNull { !it.halal_status.isNullOrBlank() }?.halal_status
        return HalalCheck(
            status = detailStatus ?: if (json.optBoolean("isHalal", true)) "halal" else "perlu verifikasi",
            notes = "Status halal berasal dari rekomendasi AI. Tetap cek label dan sertifikasi resmi bila tersedia."
        )
    }

    private fun parseGlobalSideEffects(
        json: JSONObject,
        medicineDetails: List<com.example.halalyticscompose.data.model.RecommendedMedicineDetail>
    ): List<String> {
        val direct = jsonArrayToList(json.optJSONArray("side_effects"))
        if (direct.isNotEmpty()) return direct

        return medicineDetails.flatMap { it.side_effects }.distinct()
    }

    private fun parseHerbalRemedies(json: JSONObject): List<com.example.halalyticscompose.data.model.HerbalRemedy> {
        val array = json.optJSONArray("herbal_remedies") ?: return emptyList()
        return (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            com.example.halalyticscompose.data.model.HerbalRemedy(
                name = item.optString("name").ifBlank { "Obat herbal" },
                description = item.optString("description").ifBlank { null },
                how_to_prepare = item.optString("how_to_prepare").ifBlank {
                    item.optString("cara_menyiapkan").ifBlank { null }
                },
                how_to_use = item.optString("how_to_use").ifBlank {
                    item.optString("cara_menggunakan").ifBlank { null }
                },
                frequency = item.optString("frequency").ifBlank {
                    item.optString("frekuensi").ifBlank { null }
                },
                duration = item.optString("duration").ifBlank {
                    item.optString("durasi").ifBlank { null }
                },
                precautions = item.optString("precautions").ifBlank {
                    item.optString("peringatan").ifBlank { null }
                }
            )
        }
    }

    private fun buildUsageInstructions(json: JSONObject): String? {
        val medicines = json.optJSONArray("recommended_medicines_list")
            ?: json.optJSONArray("medicines")
            ?: return null
        if (medicines.length() == 0) return null
        return (0 until medicines.length()).map { i ->
            val med = medicines.getJSONObject(i)
            val notes = med.optString("how_to_take").ifBlank {
                med.optString("notes").ifBlank { "Ikuti aturan pakai" }
            }
            "${med.optString("name")}: $notes"
        }.joinToString("\n")
    }

    private fun buildDosageGuidelines(json: JSONObject): String? {
        val medicines = json.optJSONArray("recommended_medicines_list")
            ?: json.optJSONArray("medicines")
            ?: return null
        if (medicines.length() == 0) return null
        return (0 until medicines.length()).map { i ->
            val med = medicines.getJSONObject(i)
            "${med.optString("name")}: ${med.optString("dosage").ifBlank { med.optString("dose", "sesuai anjuran") }}"
        }.joinToString(", ")
    }

    private fun buildFrequencyInfo(json: JSONObject): String? {
        val medicines = json.optJSONArray("recommended_medicines_list")
            ?: json.optJSONArray("medicines")
            ?: return null
        if (medicines.length() == 0) return null
        return (0 until medicines.length()).map { i ->
            val med = medicines.getJSONObject(i)
            "${med.optString("name")}: ${med.optString("when_to_take").ifBlank { med.optString("frequency", "sesuai anjuran") }}"
        }.joinToString(", ")
    }
}
