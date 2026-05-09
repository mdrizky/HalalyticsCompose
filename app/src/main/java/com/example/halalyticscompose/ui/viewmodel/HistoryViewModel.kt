package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import javax.inject.Inject
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _scanHistory = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    val scanHistory: StateFlow<List<ScanHistoryItem>> = _scanHistory.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    val dailyHealthScore: StateFlow<Int> = _scanHistory.map { history ->
        var score = 50 // Base score
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        history.filter { it.createdAt != null && it.createdAt.startsWith(todayStr) }.forEach { item ->
            when (item.halalStatus?.lowercase()) {
                "halal" -> score += 10
                "haram" -> score -= 15
                "syubhat" -> score -= 5
            }
        }
        score.coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    private val _totalScans = MutableStateFlow(0)
    val totalScans: StateFlow<Int> = _totalScans.asStateFlow()

    private val _halalProducts = MutableStateFlow(0)
    val halalProducts: StateFlow<Int> = _halalProducts.asStateFlow()

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private val _recommendedProducts = MutableStateFlow<List<ProductInfo>>(emptyList())
    val recommendedProducts: StateFlow<List<ProductInfo>> = _recommendedProducts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var syncJob: Job? = null

    init {
        loadBanners()
        refreshAll()
        startRealtimeSync()
    }

    fun loadBanners() {
        viewModelScope.launch {
            try {
                val response = apiService.getBanners()
                if (response.isSuccessful && response.body()?.success == true) {
                    _banners.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Load banners error", e)
            }
        }
    }

    fun refreshAll() {
        val token = sessionManager.getAuthToken() ?: return
        fetchHistory(token)
        fetchUserStats(token)
        fetchRecommendations(token)
    }

    private fun fetchRecommendations(token: String) {
        viewModelScope.launch {
            try {
                // Fetch for a default category, or general
                val response = apiService.getRecommendations("Bearer $token", "food")
                if (response.response_code == 200) {
                    _recommendedProducts.value = response.content
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Fetch recommendations error", e)
            }
        }
    }

    fun startRealtimeSync() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            while (true) {
                val token = sessionManager.getAuthToken()
                if (!token.isNullOrBlank()) {
                    fetchHistory(token)
                    fetchUserStats(token)
                }
                delay(60000) // Sync every 60 seconds
            }
        }
    }

    private fun fetchHistory(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getRealtimeScanHistory("Bearer $token")
                if (response.success) {
                    _scanHistory.value = response.data?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Fetch history error", e)
            }
        }
    }

    private fun fetchUserStats(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getUserStats("Bearer $token")
                if (response.responseCode == 200) {
                    _totalScans.value = response.content.totalScans
                    _halalProducts.value = response.content.halalScans
                    _currentStreak.value = response.content.streak?.current ?: 0
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Fetch stats error", e)
            }
        }
    }

    fun refreshHistory() {
        refreshAll()
    }

    override fun onCleared() {
        super.onCleared()
        syncJob?.cancel()
    }
}
