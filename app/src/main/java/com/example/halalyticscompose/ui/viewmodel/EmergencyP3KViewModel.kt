package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.EmergencyRequest
import com.example.halalyticscompose.data.model.EmergencyResponse
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyP3KViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _guidance = MutableStateFlow<List<String>>(emptyList())
    val guidance: StateFlow<List<String>> = _guidance.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun triggerEmergency(emergencyType: String, lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _guidance.value = emptyList() // Clear previous
            
            try {
                val userIdPattern = sessionManager.getUserId()
                if (userIdPattern <= 0) {
                    _error.value = "Unauthorized"
                    _isLoading.value = false
                    return@launch
                }
                val bearer = sessionManager.getBearerToken()
                if (bearer.isNullOrBlank()) {
                    _error.value = "Unauthorized"
                    _isLoading.value = false
                    return@launch
                }
                
                val req = EmergencyRequest(
                    userId = userIdPattern,
                    emergencyType = emergencyType,
                    latitude = lat,
                    longitude = lng
                )
                
                val res = apiService.triggerEmergency(bearer, req)
                if (res.success) {
                    _guidance.value = res.guidance
                } else {
                    _error.value = "Gagal memanggil AI Darurat. Hubungi 119!"
                }
            } catch (e: Exception) {
                _error.value = "Network Error! Segera telepon 119!"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
