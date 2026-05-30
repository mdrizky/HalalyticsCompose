package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.AiReportResponse
import com.example.halalyticscompose.data.model.WeeklyStats
import com.example.halalyticscompose.data.model.AiInsight
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

data class AiReportUiState(
    val isLoading: Boolean = false,
    val stats: WeeklyStats? = null,
    val insight: AiInsight? = null,
    val reports: List<MedicalReportHistory> = emptyList(),
    val errorMessage: String? = null
)

data class MedicalReportHistory(
    val id: Int,
    val type: String,
    val created_at: String,
    val ai_response: AiReportResponseData
)

data class AiReportResponseData(
    val status_fisik: String? = null,
    val target_2_bulan: String? = null,
    val saran_nutrisi: List<String>? = null,
    val saran_olahraga: List<String>? = null,
    val pesan_motivasi: String? = null
)

@HiltViewModel
class AiReportViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiReportUiState())
    val uiState: StateFlow<AiReportUiState> = _uiState.asStateFlow()

    fun fetchWeeklyReport(days: Int = 7) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            
            try {
                val token = sessionManager.getBearerToken() 
                    ?: sessionManager.getAuthToken()?.let { "Bearer $it" } 
                    ?: throw Exception("No authentication token")
                
                Log.d("AiReportVM", "Fetching weekly report with token: ${token.take(20)}...")
                
                val response = apiService.getWeeklyReport(token, days)
                
                if (response.success) {
                    // Check if insight has an error
                    val insightData = if (response.insight?.error != null || response.insight?.summary == null) {
                        // Provide fallback insight
                        AiInsight(
                            summary = "Analisis AI tidak tersedia saat ini. Silakan coba lagi nanti.",
                            tips = listOf("Terus pindai produk untuk data yang lebih akurat."),
                            highlight = "Tetap Semangat!"
                        )
                    } else {
                        response.insight
                    }
                    
                    
                    // Fetch History of AI Medical Reports
                    var reportsHistory: List<MedicalReportHistory> = emptyList()
                    try {
                        val reportsResponse = apiService.getMedicalReportsHistory(token)
                        if (reportsResponse.success && reportsResponse.data != null) {
                            @Suppress("UNCHECKED_CAST")
                            reportsHistory = reportsResponse.data.map {
                                val aiResponseMap = it["ai_response"] as? Map<*, *>
                                MedicalReportHistory(
                                    id = (it["id"] as? Number)?.toInt() ?: 0,
                                    type = it["type"] as? String ?: "",
                                    created_at = it["created_at"] as? String ?: "",
                                    ai_response = AiReportResponseData(
                                        status_fisik = aiResponseMap?.get("status_fisik") as? String,
                                        target_2_bulan = aiResponseMap?.get("target_2_bulan") as? String,
                                        saran_nutrisi = aiResponseMap?.get("saran_nutrisi") as? List<String>,
                                        saran_olahraga = aiResponseMap?.get("saran_olahraga") as? List<String>,
                                        pesan_motivasi = aiResponseMap?.get("pesan_motivasi") as? String
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AiReportVM", "Error fetching medical reports history", e)
                    }

                    _uiState.value = AiReportUiState(
                        isLoading = false,
                        stats = response.stats,
                        insight = insightData,
                        reports = reportsHistory
                    )
                    Log.d("AiReportVM", "Report loaded successfully")
                } else {
                    _uiState.value = AiReportUiState(
                        isLoading = false,
                        errorMessage = response.message ?: "Failed to load report"
                    )
                }
            } catch (e: Exception) {
                Log.e("AiReportVM", "Error fetching report", e)
                
                // Provide fallback data so the screen always shows something
                _uiState.value = AiReportUiState(
                    isLoading = false,
                    stats = WeeklyStats(
                        totalScans = 0,
                        halalCount = 0,
                        haramCount = 0,
                        syubhatCount = 0,
                        healthyCount = 0,
                        unhealthyCount = 0,
                        healthScore = 0,
                        topCategories = emptyMap()
                    ),
                    insight = AiInsight(
                        summary = "Belum ada data scan minggu ini. Mulai scan produk untuk mendapatkan laporan AI yang akurat!",
                        tips = listOf(
                            "Scan produk makanan untuk cek status halal",
                            "Periksa komposisi bahan untuk keamanan kesehatan",
                            "Gunakan fitur analisis AI untuk rekomendasi personal"
                        ),
                        highlight = "Mulai Perjalanan Sehat Anda!"
                    ),
                    errorMessage = null
                )
            }
        }
    }
}
