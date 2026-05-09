package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _isAdmin = MutableStateFlow(sessionManager.getRole()?.equals("admin", ignoreCase = true) == true)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(sessionManager.getAuthToken())
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    init {
        if (_isLoggedIn.value) {
            loadUserProfile()
        }
    }

    fun login(request: LoginRequest, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.login(request)
                handleAuthResponse(response, onSuccess)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error", e)
                _errorMessage.value = "Koneksi bermasalah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.googleLogin(idToken)
                handleAuthResponse(response, onSuccess)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Login error", e)
                _errorMessage.value = "Koneksi Google bermasalah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginWithFacebook(accessToken: String, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.facebookLogin(accessToken)
                handleAuthResponse(response, onSuccess)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Facebook Login error", e)
                _errorMessage.value = "Koneksi Facebook bermasalah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleAuthResponse(
        response: retrofit2.Response<LoginResponse>,
        onSuccess: (LoginResponse) -> Unit
    ) {
        if (response.isSuccessful && response.body()?.success == true) {
            val loginData = response.body()!!
            val user = loginData.user
            if (user != null) {
                sessionManager.saveAuthToken(loginData.token ?: "")
                sessionManager.saveUserId(user.id_user)
                sessionManager.saveUser(user.full_name ?: user.username, user.email)
                sessionManager.saveRole(user.role)
                
                _isLoggedIn.value = true
                _accessToken.value = loginData.token
                _isAdmin.value = user.role.equals("admin", ignoreCase = true)
                _userData.value = user.toUser()
                
                onSuccess(loginData)
            }
        } else {
            _errorMessage.value = response.body()?.message ?: "Autentikasi gagal."
        }
    }

    fun register(request: RegisterRequest, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.register(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()!!
                    onSuccess(loginData)
                } else {
                    _errorMessage.value = response.body()?.message ?: "Registrasi gagal."
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Register error", e)
                _errorMessage.value = "Koneksi bermasalah: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
                    _isAdmin.value = user?.role?.equals("admin", ignoreCase = true) == true
                    
                    // Sync with SessionManager to ensure AI analysis has latest data
                    user?.let {
                        sessionManager.saveUserData(
                            userId = it.idUser,
                            username = it.username,
                            fullName = it.fullName,
                            email = it.email,
                            phone = it.phone,
                            bloodType = it.bloodType,
                            allergy = it.allergy,
                            medicalHistory = it.medicalHistory,
                            role = it.role,
                            imageUrl = it.image,
                            gender = it.gender
                        )
                        
                        sessionManager.saveHealthProfile(
                            age = it.age,
                            height = it.height?.toFloat(),
                            weight = it.weight?.toFloat(),
                            bmi = it.bmi?.toFloat(),
                            activityLevel = it.activityLevel,
                            dietPreference = it.dietPreference,
                            goal = it.goal,
                            gender = it.gender
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Load profile error", e)
            }
        }
    }

    fun updateProfile(
        fullName: String? = null,
        email: String? = null,
        phone: String? = null,
        bloodType: String? = null,
        allergy: String? = null,
        medicalHistory: String? = null,
        height: Double? = null,
        weight: Double? = null,
        age: Int? = null,
        dietPreference: String? = null,
        language: String? = null,
        goal: String? = null,
        activityLevel: String? = null,
        gender: String? = null,
        bio: String? = null,
        address: String? = null,
        emergencyContact: String? = null,
        birthDate: String? = null,
        image: java.io.File? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = _accessToken.value ?: return@launch
                
                val response = if (image != null) {
                    val imageBody = image.asRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("image", image.name, imageBody)
                    
                    apiService.updateProfileMultipart(
                        bearer = "Bearer $token",
                        image = imagePart,
                        fullName = fullName?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        email = email?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        phone = phone?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        bloodType = bloodType?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        allergy = allergy?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        medicalHistory = medicalHistory?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        age = age?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        height = height?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        weight = weight?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        dietPreference = dietPreference?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        language = language?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        goal = goal?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        activityLevel = activityLevel?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        gender = gender?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        bio = bio?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        address = address?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        emergencyContact = emergencyContact?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        birthDate = birthDate?.toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                } else {
                    apiService.updateProfile(
                        bearer = "Bearer $token",
                        fullName = fullName,
                        email = email,
                        phone = phone,
                        bloodType = bloodType,
                        allergy = allergy,
                        medicalHistory = medicalHistory,
                        age = age,
                        height = height,
                        weight = weight,
                        dietPreference = dietPreference,
                        language = language,
                        goal = goal,
                        activityLevel = activityLevel,
                        gender = gender,
                        bio = bio,
                        address = address,
                        emergencyContact = emergencyContact,
                        birthDate = birthDate
                    )
                }
                
                if (response.isSuccess) {
                    loadUserProfile() // Refresh local state from server
                    onSuccess()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal memperbarui profil"
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Update profile failed", e)
                onError(e.message ?: "Terjadi kesalahan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        sessionManager.logout()
        _isLoggedIn.value = false
        _accessToken.value = null
        _userData.value = null
        _isAdmin.value = false
        onComplete()
    }

    fun changePassword(
        current: String,
        new: String,
        confirm: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = _accessToken.value ?: return@launch
                
                val response = apiService.changePassword(
                    bearer = "Bearer $token",
                    current = current,
                    new = new,
                    confirm = confirm
                )
                
                if (response.success) {
                    onSuccess()
                } else {
                    onError(response.message ?: "Gagal mengganti password")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Change password failed", e)
                onError(e.message ?: "Terjadi kesalahan")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
