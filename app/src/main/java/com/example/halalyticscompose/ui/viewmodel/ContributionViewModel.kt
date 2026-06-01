package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
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
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class ContributionViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _approvedCount = MutableStateFlow(0)
    val approvedCount: StateFlow<Int> = _approvedCount.asStateFlow()

    init {
        loadContributionStats()
    }

    fun loadContributionStats() {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken() ?: return@launch
            try {
                val response = apiService.getUserStats("Bearer $token")
                if (response.responseCode == 200) {
                    // Assuming user stats contains contribution info
                    // If not, we could add a specific API for this
                    _approvedCount.value = response.content.totalScans // Mocking for now if not available
                }
            } catch (e: Exception) {
                Log.e("ContributionVM", "Failed to load stats", e)
            }
        }
    }

    fun submitContribution(
        productName: String,
        barcode: String?,
        complaint: String?,
        imageFile: java.io.File?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null
                
                val token = sessionManager.getAuthToken() ?: throw Exception("Not logged in")
                
                val namePart = productName.toRequestBody("text/plain".toMediaTypeOrNull())
                val barcodePart = barcode?.toRequestBody("text/plain".toMediaTypeOrNull())
                val complaintPart = complaint?.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val imagePart = imageFile?.let { file ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val response = apiService.submitContribution(
                    bearer = "Bearer $token",
                    image = imagePart,
                    productName = namePart,
                    barcode = barcodePart,
                    complaint = complaintPart
                )
                
                if (response.success) {
                    _successMessage.value = response.message
                    onSuccess()
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e("ContributionVM", "Submit failed", e)
                _errorMessage.value = e.message ?: "Gagal mengirim kontribusi"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
