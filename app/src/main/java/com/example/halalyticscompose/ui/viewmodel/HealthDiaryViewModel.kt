package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.HealthMetricData
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HealthDiaryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _entries = MutableStateFlow<List<HealthMetricData>>(emptyList())
    val entries: StateFlow<List<HealthMetricData>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun token(): String? = sessionManager.getBearerToken()

    fun loadEntries(limit: Int = 30) {
        viewModelScope.launch {
            val bearer = token()
            if (bearer.isNullOrBlank()) {
                _error.value = "Sesi login tidak ditemukan"
                return@launch
            }

            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getHealthDiary(bearer, limit)
                if (response.success) {
                    _entries.value = response.data?.sortedByDescending { it.recordedAt } ?: emptyList()
                } else {
                    _error.value = "Gagal memuat diary kesehatan"
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat diary: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveEntry(mood: String, note: String) {
        viewModelScope.launch {
            val bearer = token()
            if (bearer.isNullOrBlank()) {
                _error.value = "Sesi login tidak ditemukan"
                return@launch
            }

            _isSaving.value = true
            _error.value = null
            try {
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                val payload = mapOf(
                    "metric_type" to "health_diary",
                    "value" to mood,
                    "recorded_at" to now,
                    "notes" to note
                )
                val response = apiService.recordHealthMetric(bearer, payload)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadEntries()
                } else {
                    _error.value = response.body()?.message ?: "Gagal menyimpan diary"
                }
            } catch (e: Exception) {
                _error.value = "Gagal menyimpan diary: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
