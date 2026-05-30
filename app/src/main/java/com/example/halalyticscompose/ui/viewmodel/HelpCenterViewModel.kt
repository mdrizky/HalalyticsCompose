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
class HelpCenterViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _categories = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val categories: StateFlow<List<Map<String, Any>>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadCategories() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = apiService.getHelpCategories("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _categories.value = response.body()?.data as? List<Map<String, Any>> ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitRequest(type: String, message: String) {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val requestData = mapOf("type" to type, "message" to message)
                val response = apiService.submitHelpRequest("Bearer $token", requestData)
                if (response.isSuccessful && response.body()?.success == true) {
                    _successMessage.value = response.body()?.message ?: "Request submitted"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _successMessage.value = null
    }
}
