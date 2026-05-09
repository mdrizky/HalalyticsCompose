package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.local.Dao.ConsumptionDao
import com.example.halalyticscompose.data.local.Entities.Consumption
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val consumptionDao: ConsumptionDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _dailyIntake = MutableStateFlow<DailyIntakeResponse?>(null)
    val dailyIntake: StateFlow<DailyIntakeResponse?> = _dailyIntake.asStateFlow()

    private val _bmi = MutableStateFlow("0.0")
    val bmi: StateFlow<String> = _bmi.asStateFlow()

    private val _aiDailyInsight = MutableStateFlow<String?>(null)
    val aiDailyInsight: StateFlow<String?> = _aiDailyInsight.asStateFlow()

    private val _healthScoreData = MutableStateFlow<HealthScoreData?>(null)
    val healthScoreData: StateFlow<HealthScoreData?> = _healthScoreData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    init {
        refreshHealthData()
        fetchCategories()
        loadBmi()
    }

    fun loadBmi() {
        val savedBmi = sessionManager.getBmi() ?: 0f
        _bmi.value = if (savedBmi > 0) String.format("%.1f", savedBmi) else "0.0"
    }

    fun refreshHealthData() {
        loadBmi()
        loadDailyIntake()
        fetchAiDailyInsight()
        fetchHealthScore()
    }

    fun updateDailyIntakeFromLocal() {
        viewModelScope.launch {
            try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val userId = sessionManager.getUserId() ?: 0

                val totalSugar = consumptionDao.getTotalSugarByDate(date, userId) ?: 0.0
                val totalSodium = consumptionDao.getTotalSodiumByDate(date, userId) ?: 0.0

                _dailyIntake.value = DailyIntakeResponse(
                    success = true,
                    message = "Computed from local database",
                    dailyIntake = DailyIntakeData(
                        totalWaterMl = 0,
                        totalCaffeineMg = 0,
                        totalSugarG = totalSugar.toInt(),
                        totalCalories = (totalSugar * 4).toInt(),
                        totalSodiumMg = totalSodium.toInt()
                    ),
                    targets = IntakeTargets(
                        waterTargetMl = 2000,
                        caffeineLimitMg = 400,
                        calorieLimit = 2000,
                        sugarLimitG = 50,
                        sodiumLimitMg = 2300,
                        carbsTargetG = 275,
                        proteinTargetG = 50,
                        fatTargetG = 70
                    )
                )
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed reading local intake DB", e)
            }
        }
    }

    fun loadDailyIntake() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getDailyIntake("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _dailyIntake.value = response.body()
                } else {
                    // Fallback to local computation if API fails
                    updateDailyIntakeFromLocal()
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to fetch daily intake", e)
                updateDailyIntakeFromLocal()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAiDailyInsight() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = apiService.getAiDailyInsight("Bearer $token")
                if (response.isSuccessful && response.body()?.status == "success") {
                    _aiDailyInsight.value = response.body()?.insight
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to fetch AI Daily Insight", e)
            }
        }
    }

    fun fetchHealthScore() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = apiService.getHealthScore("Bearer $token")
                if (response.isSuccessful && response.body()?.status == "success") {
                    _healthScoreData.value = response.body()?.data
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to fetch Health Score", e)
            }
        }
    }

    fun recordConsumption(productName: String, sugar: Double, sodium: Double, isHalal: Boolean) {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val userId = sessionManager.getUserId() ?: 0
            consumptionDao.insertConsumption(
                Consumption(
                    date = date,
                    productName = productName,
                    totalSugar = sugar,
                    totalSodium = sodium,
                    isHalal = isHalal,
                    userId = userId
                )
            )
            updateDailyIntakeFromLocal()
        }
    }
    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = apiService.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    _categories.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to fetch categories", e)
            }
        }
    }
}
