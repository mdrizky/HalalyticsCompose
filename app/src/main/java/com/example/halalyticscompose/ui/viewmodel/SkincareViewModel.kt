package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.api.BeautyProduct
import com.example.halalyticscompose.data.api.bestId
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.data.network.ApiConfig
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SkincareViewModel @Inject constructor(
    private val apiService: ApiService,
    private val openBeautyFactsApiService: com.example.halalyticscompose.data.api.OpenBeautyFactsApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    companion object {
        private const val MAX_PAGES_TO_FETCH = 3
    }

    private fun sanitizeErrorMessage(raw: String?): String? {
        val message = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val lower = message.lowercase()
        return if (
            "sqlstate" in lower ||
            "base table or view not found" in lower ||
            "syntax error or access violation" in lower
        ) {
            "Layanan data sedang bermasalah. Silakan coba lagi."
        } else {
            message
        }
    }

    private val _searchResults = MutableStateFlow<List<BeautyProduct>>(emptyList())
    val searchResults: StateFlow<List<BeautyProduct>> = _searchResults

    private val _selectedProduct = MutableStateFlow<BeautyProduct?>(null)
    val selectedProduct: StateFlow<BeautyProduct?> = _selectedProduct

    private val _analysisResult = MutableStateFlow<SkincareAnalysis?>(null)
    val analysisResult: StateFlow<SkincareAnalysis?> = _analysisResult

    private val _ingredientIndicators = MutableStateFlow<List<IngredientIndicator>>(emptyList())
    val ingredientIndicators: StateFlow<List<IngredientIndicator>> = _ingredientIndicators

    private val _analysisSummary = MutableStateFlow<String?>(null)
    val analysisSummary: StateFlow<String?> = _analysisSummary

    private val _analysisDisclaimer = MutableStateFlow<String?>(null)
    val analysisDisclaimer: StateFlow<String?> = _analysisDisclaimer

    private val _safetyResult = MutableStateFlow<SafetyCheckResponse?>(null)
    val safetyResult: StateFlow<SafetyCheckResponse?> = _safetyResult

    private val _halalResult = MutableStateFlow<HalalCheckSkincareResponse?>(null)
    val halalResult: StateFlow<HalalCheckSkincareResponse?> = _halalResult

    private val _sessionInfo = MutableStateFlow<SessionInfo?>(null)
    val sessionInfo: StateFlow<SessionInfo?> = _sessionInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _ingredientsText = MutableStateFlow<String?>(null)
    val ingredientsText: StateFlow<String?> = _ingredientsText

    private fun getToken(): String {
        return sessionManager.getAuthToken() ?: ""
    }

    private fun formatApiError(prefix: String, throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                val raw = throwable.response()?.errorBody()?.string()
                val messageFromApi = runCatching {
                    if (raw.isNullOrBlank()) null else sanitizeErrorMessage(JSONObject(raw).optString("message"))
                }.getOrNull()
                if (!messageFromApi.isNullOrBlank()) {
                    "$prefix (${throwable.code()}): $messageFromApi"
                } else {
                    "$prefix (${throwable.code()}): Layanan data tidak tersedia."
                }
            }
            is IOException -> "$prefix: Koneksi internet/API internasional tidak tersedia."
            else -> "$prefix: ${throwable.message ?: "Terjadi kesalahan tak dikenal"}"
        }
    }

    // New Function: Search OpenBeautyFacts
    fun searchSkincare(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val merged = mutableListOf<BeautyProduct>()

                for (page in 1..MAX_PAGES_TO_FETCH) {
                    val response = openBeautyFactsApiService.searchBeautyProducts(query = query, page = page, pageSize = 100)
                    if (!response.isSuccessful || response.body() == null) {
                        if (page == 1) {
                            val errorBody = sanitizeErrorMessage(response.errorBody()?.string()?.take(220))
                            _errorMessage.value = if (!errorBody.isNullOrBlank()) {
                                "Pencarian gagal (${response.code()}): $errorBody"
                            } else {
                                "Pencarian gagal (${response.code()}): ${response.message()}"
                            }
                        }
                        break
                    }
                    val body = response.body()
                    if (body == null) break
                    val products = body.products
                    if (products.isEmpty()) break
                    merged += products
                    if (products.size < 100) break
                }

                if (_errorMessage.value == null) {
                    _searchResults.value = dedupeProducts(merged)
                    if (_searchResults.value.isEmpty()) {
                        _errorMessage.value = "Data skincare tidak ditemukan dari OpenBeautyFacts."
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = formatApiError("Error koneksi OpenBeautyFacts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun dedupeProducts(products: List<BeautyProduct>): List<BeautyProduct> {
        val merged = LinkedHashMap<String, BeautyProduct>()
        products.forEach { item ->
            val key = item.bestId?.takeIf { it.isNotBlank() }
                ?: item.productName?.trim()?.lowercase()
                ?: return@forEach
            merged.putIfAbsent(key, item)
        }
        return merged.values.toList()
    }

    fun analyzeIngredients(
        ingredientsText: String? = null,
        imageBase64: String? = null,
        productName: String? = null,
        familyId: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = apiService.analyzeSkincareIngredients(
                    bearer = "Bearer ${getToken()}",
                    ingredientsText = ingredientsText,
                    imageBase64 = imageBase64,
                    productName = productName,
                    barcode = null,
                    familyId = familyId
                )
                if (response.success) {
                    _analysisResult.value = response.analysis
                    _ingredientIndicators.value = response.ingredientsDetected ?: emptyList()
                    _analysisSummary.value = response.summary ?: response.analysis?.ringkasan
                    _analysisDisclaimer.value = response.disclaimer ?: response.analysis?.disclaimer
                    _sessionInfo.value = response.sessionInfo
                    _ingredientsText.value = response.ingredientsText
                    
                    if (ingredientsText != null) {
                        checkSafety(ingredientsText)
                        checkHalal(ingredientsText)
                    }
                } else {
                    _errorMessage.value = response.message ?: "Analisis gagal"
                }
            } catch (e: Exception) {
                _errorMessage.value = formatApiError("Gagal analisis skincare", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkSafety(ingredientsText: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val response = apiService.checkSkincareSafety("Bearer ${getToken()}", ingredientsText)
                _safetyResult.value = response
            } catch (e: Exception) {
                _errorMessage.value = formatApiError("Gagal cek keamanan skincare", e)
            }
        }
    }

    fun checkHalal(ingredientsText: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                val response = apiService.checkSkincareHalal("Bearer ${getToken()}", ingredientsText)
                _halalResult.value = response
            } catch (e: Exception) {
                _errorMessage.value = formatApiError("Gagal cek halal skincare", e)
            }
        }
    }


    fun clearResults() {
        _analysisResult.value = null
        _ingredientIndicators.value = emptyList()
        _analysisSummary.value = null
        _analysisDisclaimer.value = null
        _safetyResult.value = null
        _halalResult.value = null
        _errorMessage.value = null
        _ingredientsText.value = null
    }

    fun selectProduct(product: BeautyProduct?) {
        _selectedProduct.value = product
    }

    fun selectProductById(productId: String) {
        if (productId.isBlank()) return
        val found = _searchResults.value.firstOrNull { it.bestId == productId }
        if (found != null) {
            _selectedProduct.value = found
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = openBeautyFactsApiService.getBeautyProductDetail(productId)
                if (response.isSuccessful) {
                    val product = response.body()?.product
                    if (product != null) {
                        _selectedProduct.value = product
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Detail kosmetik tidak ditemukan."
                    }
                } else {
                    val errorBody = sanitizeErrorMessage(response.errorBody()?.string()?.take(220))
                    _errorMessage.value = if (!errorBody.isNullOrBlank()) {
                        "Gagal memuat detail kosmetik (${response.code()}): $errorBody"
                    } else {
                        "Gagal memuat detail kosmetik (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = formatApiError("Error koneksi OpenBeautyFacts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
