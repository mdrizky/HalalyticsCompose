package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.FamilyProfile
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
class FamilyViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _familyProfiles = MutableStateFlow<List<FamilyProfile>>(emptyList())
    val familyProfiles: StateFlow<List<FamilyProfile>> = _familyProfiles.asStateFlow()

    private val _selectedProfile = MutableStateFlow<FamilyProfile?>(null)
    val selectedProfile: StateFlow<FamilyProfile?> = _selectedProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchFamilyProfiles()
    }

    fun selectProfile(profile: FamilyProfile?) {
        _selectedProfile.value = profile
    }

    fun fetchFamilyProfiles() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = sessionManager.getAuthToken() ?: return@launch
                val response = apiService.getFamilyProfiles("Bearer $token")
                if (response.success) {
                    _familyProfiles.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("FamilyVM", "Failed to fetch family profiles", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFamilyProfile(
        name: String,
        relationship: String?,
        age: Int?,
        gender: String?,
        allergies: String?,
        medicalHistory: String?,
        image: java.io.File? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = sessionManager.getAuthToken() ?: return@launch
                
                val response = if (image != null) {
                    val imageBody = image.asRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("image", image.name, imageBody)
                    
                    apiService.addFamilyProfileMultipart(
                        bearer = "Bearer $token",
                        image = imagePart,
                        name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                        relation = (relationship ?: "").toRequestBody("text/plain".toMediaTypeOrNull()),
                        birthDate = age?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        gender = gender?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        allergy = allergies?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        medicalHistory = medicalHistory?.toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                } else {
                    val profileMap = mutableMapOf<String, Any>(
                        "name" to name
                    )
                    relationship?.let { profileMap["relation"] = it }
                    age?.let { profileMap["birth_date"] = it }
                    gender?.let { profileMap["gender"] = it }
                    allergies?.let { profileMap["allergy"] = it }
                    medicalHistory?.let { profileMap["medical_history"] = it }

                    apiService.addFamilyProfile(
                        bearer = "Bearer $token",
                        profile = profileMap
                    )
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchFamilyProfiles()
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Gagal menambah profil keluarga")
                }
            } catch (e: Exception) {
                Log.e("FamilyVM", "Add failed", e)
                onError(e.message ?: "Gagal menambah profil keluarga")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFamilyProfile(
        id: Int,
        name: String,
        relationship: String?,
        age: Int?,
        gender: String?,
        allergies: String?,
        medicalHistory: String?,
        image: java.io.File? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = sessionManager.getAuthToken() ?: return@launch
                
                val response = if (image != null) {
                    val imageBody = image.asRequestBody("image/*".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("image", image.name, imageBody)
                    
                    apiService.updateFamilyProfileMultipart(
                        bearer = "Bearer $token",
                        id = id,
                        method = "PUT".toRequestBody("text/plain".toMediaTypeOrNull()),
                        image = imagePart,
                        name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                        relation = relationship?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        birthDate = age?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        gender = gender?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        allergy = allergies?.toRequestBody("text/plain".toMediaTypeOrNull()),
                        medicalHistory = medicalHistory?.toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                } else {
                    val profileMap = mutableMapOf<String, Any>(
                        "_method" to "PUT",
                        "name" to name
                    )
                    relationship?.let { profileMap["relation"] = it }
                    age?.let { profileMap["birth_date"] = it }
                    gender?.let { profileMap["gender"] = it }
                    allergies?.let { profileMap["allergy"] = it }
                    medicalHistory?.let { profileMap["medical_history"] = it }

                    apiService.updateFamilyProfile(
                        bearer = "Bearer $token",
                        id = id,
                        profile = profileMap
                    )
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchFamilyProfiles()
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Gagal memperbarui profil keluarga")
                }
            } catch (e: Exception) {
                Log.e("FamilyVM", "Update failed", e)
                onError(e.message ?: "Gagal memperbarui profil keluarga")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFamilyProfile(id: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = sessionManager.getAuthToken() ?: return@launch
                val response = apiService.deleteFamilyProfile("Bearer $token", id)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchFamilyProfiles()
                    if (_selectedProfile.value?.id == id) {
                        _selectedProfile.value = null
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("FamilyVM", "Delete failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
