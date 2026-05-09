package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.AdminProduct
import com.example.halalyticscompose.data.model.DashboardStats
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats.asStateFlow()

    private val _pendingProducts = MutableStateFlow<List<AdminProduct>>(emptyList())
    val pendingProducts: StateFlow<List<AdminProduct>> = _pendingProducts.asStateFlow()

    private val _actionResult = MutableStateFlow<String?>(null)
    val actionResult: StateFlow<String?> = _actionResult.asStateFlow()

    private val _users = MutableStateFlow<List<com.example.halalyticscompose.data.model.User>>(emptyList())
    val users: StateFlow<List<com.example.halalyticscompose.data.model.User>> = _users.asStateFlow()

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = apiService.getDashboardStats("Bearer $token")
                    if (response.success) {
                        _dashboardStats.value = response.data
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load stats: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun loadPendingProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = apiService.getPendingProducts("Bearer $token")
                    if (response.success) {
                        _pendingProducts.value = response.data
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load products: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = apiService.getAllUsers("Bearer $token")
                    if (response.success) {
                        _users.value = response.data
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Failed to load users: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun updateUser(id: Int, data: Map<String, Any?>) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = apiService.updateUserByAdmin("Bearer $token", id, data)
                    if (response.success) {
                        _actionResult.value = "User Updated!"
                        loadUsers()
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Update failed: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun approveProduct(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = apiService.approveProduct("Bearer $token", productId)
                    if (response.success) {
                        _actionResult.value = "Product Approved!"
                        _pendingProducts.value = _pendingProducts.value.filter { it.idProduct != productId }
                        loadDashboardData()
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Approval failed: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun rejectProduct(productId: Int, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val token = sessionManager.getAuthToken()
            if (token != null) {
                try {
                    val response = apiService.rejectProduct("Bearer $token", productId, mapOf("reason" to reason))
                    if (response.success) {
                        _actionResult.value = "Product Rejected"
                        _pendingProducts.value = _pendingProducts.value.filter { it.idProduct != productId }
                        loadDashboardData()
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Rejection failed: ${e.message}"
                }
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _errorMessage.value = null
        _actionResult.value = null
    }
}
