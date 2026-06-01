package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ProductRequestViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uploadStatus = MutableStateFlow<Result<String>?>(null)
    val uploadStatus: StateFlow<Result<String>?> = _uploadStatus

    // Helper to get apiService if needed in Composable
    fun getApiServiceHack(): ApiService = apiService

    fun uploadProductRequest(
        imageFront: MultipartBody.Part,
        imageBack: MultipartBody.Part,
        barcode: RequestBody,
        productName: RequestBody,
        ocrText: RequestBody?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _uploadStatus.value = null
            try {
                val token = "Bearer ${sessionManager.getAuthToken()}"
                val response = apiService.uploadProductRequest(
                    token,
                    imageFront,
                    imageBack,
                    barcode,
                    productName,
                    ocrText
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uploadStatus.value = Result.success("Permintaan berhasil dikirim! Admin akan memverifikasi data Anda.")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = try {
                        val json = org.json.JSONObject(errorBody ?: "")
                        json.optString("message", "Gagal mengirim permintaan")
                    } catch (e: Exception) {
                        response.body()?.message ?: "Gagal mengirim permintaan"
                    }
                    _uploadStatus.value = Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                _uploadStatus.value = Result.failure(Exception("Koneksi bermasalah: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
