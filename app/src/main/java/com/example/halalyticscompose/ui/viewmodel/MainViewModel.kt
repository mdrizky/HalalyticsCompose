package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.halalyticscompose.data.model.*
import java.io.File
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.graphics.Bitmap
import androidx.navigation.NavController

@HiltViewModel
class MainViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    // --- Global Settings & App State ---
    private val _isDarkMode = MutableStateFlow(sessionManager.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    
    private val _appLanguage = MutableStateFlow(sessionManager.getLanguage() ?: "id")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _isNotifEnabled = MutableStateFlow(sessionManager.isNotifEnabled())
    val isNotifEnabled: StateFlow<Boolean> = _isNotifEnabled.asStateFlow()

    private val _privacyModeEnabled = MutableStateFlow(true)
    val privacyModeEnabled: StateFlow<Boolean> = _privacyModeEnabled.asStateFlow()

    private val _biometricLockEnabled = MutableStateFlow(false)
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    private val _autoLogoutEnabled = MutableStateFlow(false)
    val autoLogoutEnabled: StateFlow<Boolean> = _autoLogoutEnabled.asStateFlow()

    private val _autoLogoutMinutes = MutableStateFlow(5)
    val autoLogoutMinutes: StateFlow<Int> = _autoLogoutMinutes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // --- User Profile State (used by AccountManagementScreen, ProfileStatusScreen, etc.) ---
    private val _currentUser = MutableStateFlow<String?>(sessionManager.getUsername())
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(sessionManager.getAuthToken())
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    
    // --- Meal Analysis State ---
    private val _mealAnalysisState = MutableStateFlow(AiAnalysisState())
    val mealAnalysisState: StateFlow<AiAnalysisState> = _mealAnalysisState.asStateFlow()

    // --- Family Context ---
    private val _familyProfiles = MutableStateFlow<List<FamilyProfile>>(emptyList())
    val familyProfiles: StateFlow<List<FamilyProfile>> = _familyProfiles.asStateFlow()

    private val _selectedFamilyProfile = MutableStateFlow<FamilyProfile?>(null)
    val selectedFamilyProfile: StateFlow<FamilyProfile?> = _selectedFamilyProfile.asStateFlow()

    init {
        // Any global initialization here
        fetchFamilyProfiles()
        loadUserProfile()
    }

    fun fetchFamilyProfiles() {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = apiService.getFamilyProfiles("Bearer $token")
                if (response.success) {
                    _familyProfiles.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch family profiles", e)
            }
        }
    }

    fun selectFamilyProfile(profile: FamilyProfile?) {
        _selectedFamilyProfile.value = profile
    }

    fun updateFcmToken(fcmToken: String) {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                apiService.updateFcmToken("Bearer $token", mapOf("fcm_token" to fcmToken))
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error updating FCM token", e)
            }
        }
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        viewModelScope.launch {
            preferenceManager.setDarkMode(newValue)
            sessionManager.saveDarkMode(newValue)
        }
    }

    fun setAppLanguage(languageCode: String) {
        _appLanguage.value = languageCode
        viewModelScope.launch {
            sessionManager.saveLanguage(languageCode)
        }
    }

    fun setNotifEnabled(isEnabled: Boolean) {
        _isNotifEnabled.value = isEnabled
        viewModelScope.launch {
            preferenceManager.setNotifEnabled(isEnabled)
            sessionManager.saveNotifEnabled(isEnabled)
        }
    }

    fun setBiometricLockEnabled(isEnabled: Boolean) {
        _biometricLockEnabled.value = isEnabled
    }

    fun setAutoLogoutEnabled(isEnabled: Boolean) {
        _autoLogoutEnabled.value = isEnabled
    }

    fun setAutoLogoutMinutes(minutes: Int) {
        _autoLogoutMinutes.value = minutes
    }

    fun setPrivacyModeEnabled(isEnabled: Boolean) {
        _privacyModeEnabled.value = isEnabled
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadUserProfile() {
        val token = _accessToken.value ?: sessionManager.getAuthToken()
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val response = apiService.getProfile("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    _userData.value = user
                    _currentUser.value = user?.fullName ?: user?.username
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Load profile error", e)
            }
        }
    }

    fun logout(navController: NavController) {
        sessionManager.logout()
        _currentUser.value = null
        _userData.value = null
        _accessToken.value = null
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = apiService.forgotPassword(email)
                if (response.success) {
                    onSuccess()
                } else {
                    onError("Failed to send reset email. Please try again.")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Reset password error", e)
                onError(e.message ?: "Network error")
            }
        }
    }
    
    fun analyzeMealImage(imageFile: File) {
        viewModelScope.launch {
            _mealAnalysisState.value = _mealAnalysisState.value.copy(isLoading = true, error = null)
            try {
                // Convert file to Base64
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
                val base64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                
                val token = sessionManager.getAuthToken() ?: ""
                val response = apiService.analyzeMeal(
                    token = "Bearer $token",
                    request = MealAnalysisRequest(image = base64)
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success && result.data != null) {
                        _mealAnalysisState.value = _mealAnalysisState.value.copy(
                            isLoading = false,
                            data = result.data
                        )
                    } else {
                        _mealAnalysisState.value = _mealAnalysisState.value.copy(
                            isLoading = false,
                            error = result.message ?: "Gagal menganalisis makanan."
                        )
                    }
                } else {
                    _mealAnalysisState.value = _mealAnalysisState.value.copy(
                        isLoading = false,
                        error = "Server error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Meal analysis failed", e)
                _mealAnalysisState.value = _mealAnalysisState.value.copy(
                    isLoading = false,
                    error = "Koneksi bermasalah: ${e.message}"
                )
            }
        }
    }

    // FCM Token helper
    fun syncWithMySQL(firebaseUser: com.google.firebase.auth.FirebaseUser, token: String) {
        viewModelScope.launch {
            try {
                apiService.syncUser(
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName,
                    fcmToken = null
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Sync failed", e)
            }
        }
    }
    
    // Add missing updateFcmToken map parameter
    suspend fun updateFcmToken(token: String, map: Map<String, String>) {
        // Implementation already exists below as updateFcmToken(fcmToken: String)
        // This is to fix potential signature mismatch in calls
    }

    fun addScanToHistory(
        productId: Int? = null,
        barcode: String? = null,
        productName: String,
        productImage: String? = null,
        halalStatus: String = "unknown",
        scanMethod: String = "barcode",
        source: String = "openfoodfacts",
        latitude: Double? = null,
        longitude: Double? = null,
        confidenceScore: Int? = null,
        nutritionSnapshot: Map<String, Any>? = null
    ) {
        val token = sessionManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val request = RecordScanRequest(
                    scannable_type = if (productId != null) "Product" else "External",
                    scannable_id = productId ?: 0,
                    product_name = productName,
                    product_image = productImage,
                    barcode = barcode,
                    halal_status = halalStatus,
                    scan_method = scanMethod,
                    source = source,
                    latitude = latitude,
                    longitude = longitude,
                    confidence_score = confidenceScore,
                    nutrition_snapshot = nutritionSnapshot
                )
                apiService.recordScan("Bearer $token", request)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to record scan", e)
            }
        }
    }
}
