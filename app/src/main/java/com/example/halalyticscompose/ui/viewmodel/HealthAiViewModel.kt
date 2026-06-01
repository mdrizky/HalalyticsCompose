package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HealthAiViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 1. Drug Interaction
    private val _interactionResult = MutableStateFlow<DrugInteractionData?>(null)
    val interactionResult: StateFlow<DrugInteractionData?> = _interactionResult.asStateFlow()
    private val _interactionSource = MutableStateFlow<String?>(null)
    val interactionSource: StateFlow<String?> = _interactionSource.asStateFlow()

    fun checkInteraction(drugAId: Int? = null, drugBId: Int? = null, drugAName: String? = null, drugBName: String? = null, familyId: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _interactionResult.value = null
                val token = getToken()
                if (token.isNullOrBlank()) {
                    _error.value = "Sesi login tidak ditemukan. Silakan login ulang."
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.checkDrugInteraction(token, drugAId, drugBId, drugAName, drugBName, familyId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _interactionResult.value = response.body()?.data
                    _interactionSource.value = response.body()?.source
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = if (response.code() == 404) "Layanan interaksi obat belum tersedia di server." else "Gagal memproses interaksi: ${response.message()}"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Gagal memproses interaksi: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // 2. Pill Identification
    private val _pillIdentifyResult = MutableStateFlow<PillIdentifyData?>(null)
    val pillIdentifyResult: StateFlow<PillIdentifyData?> = _pillIdentifyResult.asStateFlow()

    fun identifyPill(imageFile: File, shape: String? = null, color: String? = null, familyId: Int? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
                val shapePart = shape?.toRequestBody("text/plain".toMediaTypeOrNull())
                val colorPart = color?.toRequestBody("text/plain".toMediaTypeOrNull())

                val token = getToken()
                val familyIdPart = familyId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val response = apiService.identifyPill(token ?: "", imagePart, shapePart, colorPart, familyIdPart)
                if (response.success) {
                    _pillIdentifyResult.value = response.data
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Gagal mengidentifikasi pil: ${e.message}"
                _isLoading.value = false
            }
        }
    }


    // 3. Medication Reminders
    private val _reminders = MutableStateFlow<List<MedicationReminderItem>>(emptyList())
    val reminders: StateFlow<List<MedicationReminderItem>> = _reminders.asStateFlow()

    fun createReminder(drugId: Int, dosage: String, frequency: String, timeSlots: List<String>, startDate: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val request = mapOf(
                    "drug_id" to drugId,
                    "dosage" to dosage,
                    "frequency" to frequency,
                    "time_slots" to timeSlots,
                    "start_date" to startDate
                )
                val token = getToken()
                apiService.createAdvancedReminder(token ?: "", request)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Gagal membuat pengingat: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // 5. Halal Alternatives
    private val _halalAlternatives = MutableStateFlow<HalalAlternativeData?>(null)
    val halalAlternatives: StateFlow<HalalAlternativeData?> = _halalAlternatives.asStateFlow()

    fun fetchHalalAlternatives(drugId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = getToken()
                val response = apiService.getHalalAlternatives(token ?: "", drugId)
                if (response.success) {
                    _halalAlternatives.value = response.data
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Gagal mencari alternatif: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    // 6. Health Metrics (Journey)
    private val _metricHistory = MutableStateFlow<List<HealthMetricData>>(emptyList())
    val metricHistory: StateFlow<List<HealthMetricData>> = _metricHistory.asStateFlow()

    fun recordMetric(type: String, value: String, date: String, notes: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = getToken()
                val body = mutableMapOf<String, Any>(
                    "metric_type" to type,
                    "value" to value,
                    "recorded_at" to date
                )
                if (!notes.isNullOrBlank()) body["notes"] = notes
                apiService.recordHealthMetric(token ?: "", body)
                fetchMetricHistory(type)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Gagal mencatat metrik: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchMetricHistory(type: String) {
        viewModelScope.launch {
            try {
                val token = getToken()
                val response = apiService.getHealthMetricHistory(token ?: "", type)
                if (response.success) {
                    _metricHistory.value = response.data
                }
            } catch (e: Exception) {
                _error.value = "Gagal mengambil riwayat: ${e.message}"
            }
        }
    }

    // Helpers
    private fun getToken(): String? {
        return sessionManager.getBearerToken()
    }

    fun clearError() {
        _error.value = null
    }
}
