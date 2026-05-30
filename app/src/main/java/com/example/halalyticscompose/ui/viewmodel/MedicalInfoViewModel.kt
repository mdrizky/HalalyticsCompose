package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.halalyticscompose.data.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.example.halalyticscompose.utils.SessionManager

@HiltViewModel
class MedicalInfoViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _profileData = MutableStateFlow<Map<String, Any>?>(null)
    val profileData: StateFlow<Map<String, Any>?> = _profileData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadProfile() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.getMedicalProfile("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data as? Map<String, Any>
                    _profileData.value = data
                } else {
                    _error.value = "Failed to load medical profile"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(data: Map<String, Any>, onSuccess: () -> Unit) {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.updateMedicalProfile("Bearer $token", data)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadProfile()
                    onSuccess()
                } else {
                    _error.value = "Failed to update profile"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
