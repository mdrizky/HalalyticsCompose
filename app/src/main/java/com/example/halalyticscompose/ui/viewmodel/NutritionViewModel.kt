package com.example.halalyticscompose.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NutritionUiState(
    val dailySummary: NutritionDashboardData? = null,
    val history: List<NutritionHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isAnalyzing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getDailyNutritionLog("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.value = _uiState.value.copy(
                        dailySummary = response.body()!!.data,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e("NutritionViewModel", "loadDashboard error", e)
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadHistory() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = apiService.getNutritionHistory("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success) {
                        _uiState.value = _uiState.value.copy(
                            history = body.data ?: emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("NutritionViewModel", "loadHistory error", e)
            }
        }
    }

    fun logMeal(imageUri: Uri, mealType: String, context: Context) {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@launch
                val file = java.io.File(context.cacheDir, "meal_${System.currentTimeMillis()}.jpg")
                file.outputStream().use { output -> inputStream.copyTo(output) }
                inputStream.close()

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = okhttp3.MultipartBody.Part.createFormData("image", file.name, requestFile)
                val mealTypePart = mealType.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.logMealMultipart(
                    "Bearer $token",
                    imagePart,
                    mealTypePart
                )
                if (response.isSuccessful) {
                    loadDashboard()
                }
            } catch (e: Exception) {
                Log.e("NutritionViewModel", "logMeal error", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isAnalyzing = false)
            }
        }
    }
}
