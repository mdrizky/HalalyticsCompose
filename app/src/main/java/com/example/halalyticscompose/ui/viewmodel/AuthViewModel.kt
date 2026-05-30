package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.work.WorkManager
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.local.HalalyticsDatabase
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.utils.RoleHelper
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager,
    private val database: HalalyticsDatabase,
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    private val _isAdmin = MutableStateFlow(RoleHelper.isAdmin(sessionManager.getRole()))
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _isNutritionist = MutableStateFlow(RoleHelper.isNutritionist(sessionManager.getRole()))
    val isNutritionist: StateFlow<Boolean> = _isNutritionist.asStateFlow()

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
        if (!response.isSuccessful) {
            val errorBodyString = response.errorBody()?.string()
            val parsedMessage = try {
                if (!errorBodyString.isNullOrEmpty()) {
                    val json = org.json.JSONObject(errorBodyString)
                    json.optString("message", json.optString("error", "Autentikasi gagal."))
                } else {
                    "Autentikasi gagal."
                }
            } catch (e: Exception) {
                "Autentikasi gagal."
            }
            _errorMessage.value = parsedMessage
            return
        }

        val body = response.body()
        val ok = body != null && body.isSuccess && body.user != null
        if (!ok) {
            _errorMessage.value = body?.message ?: body?.errorMessage ?: "Autentikasi gagal."
            return
        }
        val loginData = body!!
        val user = loginData.user!!
        if (loginData.token.isNullOrBlank()) {
            _errorMessage.value = "Token autentikasi tidak diterima dari server."
            return
        }
        sessionManager.saveCompleteSession(
            token = loginData.token,
            userId = user.id_user,
            username = user.username,
            fullName = user.full_name,
            email = user.email,
            role = user.role,
            phone = user.phone,
            bloodType = user.blood_type,
            allergy = user.allergy,
            medicalHistory = user.medical_history,
            imageUrl = user.image
        )

        // Save health profile
        sessionManager.saveHealthProfile(
            age = user.age,
            height = user.height?.toFloat(),
            weight = user.weight?.toFloat(),
            bmi = user.bmi?.toFloat(),
            activityLevel = user.activity_level,
            dietPreference = user.diet_preference,
            goal = user.goal,
            gender = user.gender
        )

        // Save stats
        sessionManager.saveStats(
            totalScans = user.total_scan,
            halalCount = user.halal_count,
            syubhatCount = user.syubhat_count,
            streak = user.streak
        )

        // Save preferences
        sessionManager.savePreferences(
            darkMode = user.dark_mode ?: false,
            notifEnabled = user.notif_enabled ?: true,
            language = user.language ?: "id"
        )
        
        sessionManager.saveLocation(
            address = user.address,
            city = null,
            province = null
        )

        _isLoggedIn.value = true
        _accessToken.value = loginData.token
        _isAdmin.value = RoleHelper.isAdmin(user.role)
        _isNutritionist.value = RoleHelper.isNutritionist(user.role)
        _userData.value = user.toUser()

        onSuccess(loginData)
    }

    fun register(request: RegisterRequest, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.register(request)
                handleAuthResponse(response, onSuccess)
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
        if (token.isNullOrBlank()) {
            _errorMessage.value = "Sesi berakhir. Silakan login kembali."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.getProfile("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    // Bust Coil cache by appending a timestamp query parameter to the image URL
                    val processedUser = user?.let {
                        val ts = System.currentTimeMillis()
                        val bustedImage = it.image?.let { img -> if (img.contains("?")) "$img&v=$ts" else "$img?v=$ts" }
                        it.copy(image = bustedImage)
                    }

                    _userData.value = processedUser
                    _isAdmin.value = RoleHelper.isAdmin(processedUser?.role)
                    _isNutritionist.value = RoleHelper.isNutritionist(processedUser?.role)
                    
                    // Sync with SessionManager to ensure AI analysis has latest data
                    processedUser?.let {
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
                } else {
                    _errorMessage.value = "Gagal memuat profil: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Load profile error", e)
                _errorMessage.value = "Koneksi bermasalah: ${e.message}"
            } finally {
                _isLoading.value = false
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
                
                if (response.success) {
                    loadUserProfile() // Refresh local state from server
                    onSuccess()
                } else {
                    val errorMsg = response.message ?: "Gagal memperbarui profil"
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
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    try {
                        WorkManager.getInstance(appContext).cancelAllWork()
                    } catch (_: Exception) {
                    }
                    try {
                        database.clearAllTables()
                    } catch (e: Exception) {
                        Log.e("AuthViewModel", "clearAllTables failed", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "logout cleanup failed", e)
            }
            sessionManager.logout()
            _isLoggedIn.value = false
            _accessToken.value = null
            _userData.value = null
            _isAdmin.value = false
            _isNutritionist.value = false
            onComplete()
        }
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

    fun getBmiAdvice(weight: Float, height: Float, onResult: (BmiAdviceData?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = _accessToken.value ?: ""
                val response = apiService.getBmiAdvice(
                    bearer = "Bearer $token",
                    request = BmiAdviceRequest(weight, height)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    onResult(response.body()?.data)
                } else {
                    _errorMessage.value = "Gagal mengambil saran AI: ${response.message()}"
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "BMI Advice error", e)
                _errorMessage.value = "Kesalahan: ${e.message}"
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
