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
import kotlinx.coroutines.withTimeout
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanResult = MutableStateFlow<ProductInfo?>(null)
    val scanResult: StateFlow<ProductInfo?> = _scanResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _mealAnalysisState = MutableStateFlow(AiAnalysisState())
    val mealAnalysisState: StateFlow<AiAnalysisState> = _mealAnalysisState.asStateFlow()

    fun searchByBarcode(barcode: String, onSuccess: (ProductInfo) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val token = sessionManager.getAuthToken()
                val response = apiService.searchByBarcode("Bearer $token", barcode)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val product = apiResponse.data
                        if (product != null) {
                            _scanResult.value = product
                            onSuccess(product)
                        } else {
                            _errorMessage.value = "Produk tidak ditemukan."
                        }
                    } else {
                        _errorMessage.value = apiResponse?.message ?: "Gagal mengambil data produk."
                    }
                } else {
                    _errorMessage.value = "Gagal mengambil data produk: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Barcode search error", e)
                _errorMessage.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeMealImage(imageFile: java.io.File) {
        viewModelScope.launch {
            _mealAnalysisState.value = AiAnalysisState(isLoading = true)
            try {
                val token = sessionManager.getAuthToken()
                if (token == null) {
                    _mealAnalysisState.value = AiAnalysisState(error = "User not logged in")
                    return@launch
                }

                // Compress image to prevent OOM
                val compressedFile = com.example.halalyticscompose.utils.ImageUtils.reduceFileImage(imageFile)
                val bytes = compressedFile.readBytes()
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

                val request = MealAnalysisRequest(image = base64)
                val response = withTimeout(30000) {
                    apiService.analyzeMeal("Bearer $token", request)
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data as? MealData
                    if (data != null) {
                        _mealAnalysisState.value = AiAnalysisState(data = data)
                    } else {
                        _mealAnalysisState.value = AiAnalysisState(error = "Empty data received")
                    }
                } else {
                    _mealAnalysisState.value = AiAnalysisState(error = response.body()?.message ?: "Analysis failed")
                }
            } catch (e: Exception) {
                Log.e("ScanViewModel", "Meal analysis error", e)
                _mealAnalysisState.value = AiAnalysisState(error = "Error: ${e.message}")
            }
        }
    }

    fun clearResult() {
        _scanResult.value = null
        _errorMessage.value = null
    }
}
