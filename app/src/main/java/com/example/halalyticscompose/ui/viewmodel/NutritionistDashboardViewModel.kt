package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NutritionistDashboardViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
) : ViewModel() {

    sealed class NutritionistUiState {
        data object Loading : NutritionistUiState()
        data class Error(val message: String) : NutritionistUiState()
        data class Loaded(
            val disclaimer: String?,
            val activeConsultations: Int,
            val totalPatients: Int,
            val obesity: Int,
            val underweight: Int,
            val otherBmi: Int,
            val recentTitles: List<String>,
        ) : NutritionistUiState()
    }

    private val _uiState = MutableStateFlow<NutritionistUiState>(NutritionistUiState.Loading)
    val uiState: StateFlow<NutritionistUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken()
            if (token.isNullOrBlank()) {
                _uiState.value = NutritionistUiState.Error("Sesi tidak valid. Silakan login ulang.")
                return@launch
            }
            _uiState.value = NutritionistUiState.Loading
            try {
                val body = apiService.getNutritionistDashboard("Bearer $token")
                if (!body.success) {
                    _uiState.value = NutritionistUiState.Error(body.message ?: "Gagal memuat dasbor")
                    return@launch
                }
                val d = body.data
                val recent = d?.recentConsultations.orEmpty().mapNotNull { c ->
                    val name = c.userName
                    val subj = c.subject ?: "Konsultasi"
                    if (!name.isNullOrBlank()) "$subj — $name" else subj
                }
                _uiState.value = NutritionistUiState.Loaded(
                    disclaimer = body.medicalDisclaimer,
                    activeConsultations = d?.activeConsultations ?: 0,
                    totalPatients = d?.totalPatientsDistinct ?: 0,
                    obesity = d?.populationBmiSnapshot?.obesityBmiGte30 ?: 0,
                    underweight = d?.populationBmiSnapshot?.underweightBmiLt185 ?: 0,
                    otherBmi = d?.populationBmiSnapshot?.otherRecordedBmi ?: 0,
                    recentTitles = recent,
                )
            } catch (e: Exception) {
                _uiState.value = NutritionistUiState.Error(e.message ?: "Kesalahan jaringan")
            }
        }
    }
}
