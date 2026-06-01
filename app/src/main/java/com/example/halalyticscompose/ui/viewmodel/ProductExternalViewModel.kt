package com.example.halalyticscompose.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.model.ProductItem
import com.example.halalyticscompose.data.network.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import org.json.JSONObject
import retrofit2.Response
import com.example.halalyticscompose.domain.usecase.GetProductImagesUseCase
import com.example.halalyticscompose.data.model.ProductImageResult

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for handling External Product API (OpenFoodFacts via Laravel backend)
 */
@HiltViewModel
class ProductExternalViewModel @Inject constructor(
    private val externalApiService: com.example.halalyticscompose.data.network.ExternalApiService,
    private val openFoodFactsApiService: com.example.halalyticscompose.data.api.OpenFoodFactsApiService,
    private val getProductImagesUseCase: GetProductImagesUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "ProductExternalVM"
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

    private fun buildHttpError(response: Response<*>, fallback: String): String {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        val parsedMessage = runCatching {
            if (raw.isNullOrBlank()) null else sanitizeErrorMessage(JSONObject(raw).optString("message"))
        }.getOrNull()
        return if (!parsedMessage.isNullOrBlank()) {
            "$fallback (${response.code()}): $parsedMessage"
        } else {
            "$fallback (${response.code()}): ${response.message().ifBlank { "Unknown error" }}"
        }
    }

    private fun buildThrowableError(prefix: String, e: Exception): String {
        return when (e) {
            is IOException -> "$prefix: API internasional sedang lambat/tidak dapat diakses. Coba lagi beberapa saat."
            else -> "$prefix: Terjadi kesalahan saat memproses pencarian."
        }
    }
    
    // ========================================
    // STATE FLOWS
    // ========================================
    
    // Search Results
    private val _searchResults = MutableStateFlow<List<ProductItem>>(emptyList())
    val searchResults: StateFlow<List<ProductItem>> = _searchResults.asStateFlow()
    
    // Product Detail
    private val _productDetail = MutableStateFlow<ProductItem?>(null)
    val productDetail: StateFlow<ProductItem?> = _productDetail.asStateFlow()
    
    // Intelligence Result
    private val _intelligenceResult = MutableStateFlow<com.example.halalyticscompose.data.model.ProductIntelligenceResult?>(null)
    val intelligenceResult: StateFlow<com.example.halalyticscompose.data.model.ProductIntelligenceResult?> = _intelligenceResult.asStateFlow()
    
    // Total count from search
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    // Images State
    private val _productImageState = MutableStateFlow<ProductImageResult?>(null)
    val productImageState: StateFlow<ProductImageResult?> = _productImageState.asStateFlow()
    
    // Loading States
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()
    
    // Error States
    private val _searchError = MutableStateFlow("")
    val searchError: StateFlow<String> = _searchError.asStateFlow()
    
    private val _detailError = MutableStateFlow("")
    val detailError: StateFlow<String> = _detailError.asStateFlow()
    
    // Current filter
    private val _currentFilter = MutableStateFlow("All")
    val currentFilter: StateFlow<String> = _currentFilter.asStateFlow()
    
    // ========================================
    // SEARCH FUNCTIONS
    // ========================================
    
    /**
     * Search all products by query
     */
    fun searchProducts(query: String, pageSize: Int = 20, page: Int = 1) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = ""
            _currentFilter.value = "All"
            
            Log.d(TAG, "🔍 Searching products: $query")
            
            try {
                val response = externalApiService.searchProducts(query = query, pageSize = pageSize, page = page)
                
                Log.d(TAG, "📡 Response code: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content
                    
                    if (body?.responseCode == 200 && content != null) {
                        val backendProducts = fetchBackendProductsPaged(query, pageSize, startPage = page)
                        val directProducts = fetchProductsDirectFallback(query, pageSize, page)
                        val mergedProducts = mergeUniqueProducts(backendProducts, directProducts)

                        _searchResults.value = mergedProducts
                        _totalCount.value = maxOf(content.count, mergedProducts.size)
                        _searchError.value = if (mergedProducts.isEmpty()) "Produk tidak ditemukan." else ""
                        Log.d(
                            TAG,
                            "✅ Found ${mergedProducts.size} products (backend: ${backendProducts.size}, direct: ${directProducts.size})"
                        )
                    } else {
                        val fallbackOk = searchProductsDirectFallback(query, pageSize, page)
                        if (!fallbackOk) {
                            _searchError.value = body?.message ?: "No products found"
                            _searchResults.value = emptyList()
                            _totalCount.value = 0
                            Log.d(TAG, "⚠️ ${body?.message}")
                        }
                    }
                } else {
                    val fallbackOk = searchProductsDirectFallback(query, pageSize, page)
                    if (!fallbackOk) {
                        val errorMsg = buildHttpError(response, "Server error")
                        _searchError.value = errorMsg
                        _searchResults.value = emptyList()
                        _totalCount.value = 0
                        Log.e(TAG, "❌ $errorMsg")
                    }
                }
            } catch (e: Exception) {
                val fallbackOk = searchProductsDirectFallback(query, pageSize, page)
                if (!fallbackOk) {
                    val errorMsg = buildThrowableError("Network error", e)
                    _searchError.value = errorMsg
                    _searchResults.value = emptyList()
                    _totalCount.value = 0
                    Log.e(TAG, "❌ $errorMsg", e)
                }
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    /**
     * Search halal products
     */
    fun searchHalalProducts(query: String = "", pageSize: Int = 20, page: Int = 1) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = ""
            _currentFilter.value = "Halal"
            
            Log.d(TAG, "🕌 Searching halal products: $query")
            
            try {
                val response = externalApiService.searchHalalProducts(query = query, pageSize = pageSize, page = page)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content
                    
                    if (body?.responseCode == 200 && content != null) {
                        _searchResults.value = content.products
                        _totalCount.value = content.count
                        Log.d(TAG, "✅ Found ${content.products.size} halal products")
                    } else {
                        _searchError.value = body?.message ?: "No halal products found"
                        _searchResults.value = emptyList()
                        _totalCount.value = 0
                    }
                } else {
                    _searchError.value = buildHttpError(response, "Server error")
                    _searchResults.value = emptyList()
                    _totalCount.value = 0
                }
            } catch (e: Exception) {
                _searchError.value = buildThrowableError("Network error", e)
                _searchResults.value = emptyList()
                _totalCount.value = 0
                Log.e(TAG, "Error searching halal products", e)
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    /**
     * Search vegetarian products
     */
    fun searchVegetarianProducts(query: String = "", pageSize: Int = 20, page: Int = 1) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = ""
            _currentFilter.value = "Vegetarian"
            
            Log.d(TAG, "🌱 Searching vegetarian products: $query")
            
            try {
                val response = externalApiService.searchVegetarianProducts(query = query, pageSize = pageSize, page = page)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content
                    
                    if (body?.responseCode == 200 && content != null) {
                        _searchResults.value = content.products
                        _totalCount.value = content.count
                        Log.d(TAG, "✅ Found ${content.products.size} vegetarian products")
                    } else {
                        _searchError.value = body?.message ?: "No vegetarian products found"
                        _searchResults.value = emptyList()
                        _totalCount.value = 0
                    }
                } else {
                    _searchError.value = buildHttpError(response, "Server error")
                    _searchResults.value = emptyList()
                    _totalCount.value = 0
                }
            } catch (e: Exception) {
                _searchError.value = buildThrowableError("Network error", e)
                _searchResults.value = emptyList()
                _totalCount.value = 0
                Log.e(TAG, "Error searching vegetarian products", e)
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    /**
     * Search vegan products
     */
    fun searchVeganProducts(query: String = "", pageSize: Int = 20, page: Int = 1) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = ""
            _currentFilter.value = "Vegan"
            
            Log.d(TAG, "🥬 Searching vegan products: $query")
            
            try {
                val response = externalApiService.searchVeganProducts(query = query, pageSize = pageSize, page = page)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content
                    
                    if (body?.responseCode == 200 && content != null) {
                        _searchResults.value = content.products
                        _totalCount.value = content.count
                        Log.d(TAG, "✅ Found ${content.products.size} vegan products")
                    } else {
                        _searchError.value = body?.message ?: "No vegan products found"
                        _searchResults.value = emptyList()
                        _totalCount.value = 0
                    }
                } else {
                    _searchError.value = buildHttpError(response, "Server error")
                    _searchResults.value = emptyList()
                    _totalCount.value = 0
                }
            } catch (e: Exception) {
                _searchError.value = buildThrowableError("Network error", e)
                _searchResults.value = emptyList()
                _totalCount.value = 0
                Log.e(TAG, "Error searching vegan products", e)
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    /**
     * Search products by brand
     */
    fun searchByBrand(brand: String, pageSize: Int = 20, page: Int = 1) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = ""
            _currentFilter.value = "Brand: $brand"
            
            Log.d(TAG, "🏷️ Searching by brand: $brand")
            
            try {
                val response = externalApiService.getProductsByBrand(brand = brand, pageSize = pageSize, page = page)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content
                    
                    if (body?.responseCode == 200 && content != null) {
                        _searchResults.value = content.products
                        _totalCount.value = content.count
                        Log.d(TAG, "✅ Found ${content.products.size} products from $brand")
                    } else {
                        _searchError.value = body?.message ?: "No products found for this brand"
                        _searchResults.value = emptyList()
                        _totalCount.value = 0
                    }
                } else {
                    _searchError.value = buildHttpError(response, "Server error")
                    _searchResults.value = emptyList()
                    _totalCount.value = 0
                }
            } catch (e: Exception) {
                _searchError.value = buildThrowableError("Network error", e)
                _searchResults.value = emptyList()
                _totalCount.value = 0
                Log.e(TAG, "Error searching by brand", e)
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    /**
     * Search products by category
     */
    fun searchByCategory(category: String, pageSize: Int = 20, page: Int = 1) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = ""
            _currentFilter.value = "Category: $category"
            
            Log.d(TAG, "📁 Searching by category: $category")
            
            try {
                val response = externalApiService.getProductsByCategory(category = category, pageSize = pageSize, page = page)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    val content = body?.content
                    
                    if (body?.responseCode == 200 && content != null) {
                        _searchResults.value = content.products
                        _totalCount.value = content.count
                        Log.d(TAG, "✅ Found ${content.products.size} products in $category")
                    } else {
                        _searchError.value = body?.message ?: "No products found in this category"
                        _searchResults.value = emptyList()
                        _totalCount.value = 0
                    }
                } else {
                    _searchError.value = buildHttpError(response, "Server error")
                    _searchResults.value = emptyList()
                    _totalCount.value = 0
                }
            } catch (e: Exception) {
                _searchError.value = buildThrowableError("Network error", e)
                _searchResults.value = emptyList()
                _totalCount.value = 0
                Log.e(TAG, "Error searching by category", e)
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    // ========================================
    // PRODUCT DETAIL
    // ========================================
    
    /**
     * Get product detail by barcode
     */
    fun getProductDetail(barcode: String) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _detailError.value = ""
            
            val cleanBarcode = barcode.trim()
            Log.d(TAG, "📦 Fetching product detail: $cleanBarcode")
            
            try {
                // Step 1: Try Local Backend First
                val backendResponse = externalApiService.getProductDetail(cleanBarcode)
                Log.d(TAG, "📡 Backend Response: ${backendResponse.code()}")
                
                if (backendResponse.isSuccessful) {
                    val body = backendResponse.body()
                    if (body?.responseCode == 200 && body.content != null) {
                        _productDetail.value = body.content
                        Log.d(TAG, "✅ Product found in Backend: ${body.content.getDisplayName()}")
                        fetchAdvancedImages(body.content.getDisplayName(), cleanBarcode)
                        performIntelligenceAnalysis(body.content)
                        _isLoadingDetail.value = false
                        return@launch
                    }
                }

                // Step 2: Fallback to OpenFoodFacts Direct
                Log.d(TAG, "🔄 Falling back to OpenFoodFacts Direct for: $cleanBarcode")
                val offResponse = openFoodFactsApiService.getProductDetail(cleanBarcode)
                
                if (offResponse.isSuccessful) {
                    val body = offResponse.body()
                    if (body?.product != null) {
                        Log.d(TAG, "✅ OFF Direct Match: ${body.product.getDisplayName()}")
                        _productDetail.value = body.product
                        fetchAdvancedImages(body.product.getDisplayName(), cleanBarcode)
                        performIntelligenceAnalysis(body.product)
                        _isLoadingDetail.value = false
                        return@launch
                    }
                }

                // Step 3: Final Fallback - If still not found
                _detailError.value = "Produk tidak ditemukan di database Halalytics maupun Open Food Facts."
                _productDetail.value = null
                Log.d(TAG, "⚠️ Product $cleanBarcode not found anywhere.")
                
            } catch (e: Exception) {
                _detailError.value = buildThrowableError("Gagal memproses data produk", e)
                _productDetail.value = null
                Log.e(TAG, "❌ Error fetching product detail", e)
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    private fun fetchAdvancedImages(productName: String, barcode: String) {
        viewModelScope.launch {
            try {
                val result = getProductImagesUseCase(productName, barcode, "external")
                _productImageState.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching advanced images", e)
            }
        }
    }
    
    private suspend fun performIntelligenceAnalysis(product: ProductItem) {
        try {
            val ingredientsText = product.ingredientsText ?: product.ingredientsTextEn ?: ""
            val productName = product.getDisplayName()

            // 1. Local Halal Database Check
            var halalAnalysis = com.example.halalyticscompose.data.local.LocalHaramDatabase.analyzeIngredients(ingredientsText)
            
            // 2. Parallel AI Calls (if needed)
            var ingredientsAnalysis: List<com.example.halalyticscompose.data.model.IngredientDetail>? = null
            var alternatives: List<com.example.halalyticscompose.data.model.AlternativeProduct>? = null
            
            kotlinx.coroutines.coroutineScope {
                val aiHalalDeferred = if (halalAnalysis == null || halalAnalysis!!.status == com.example.halalyticscompose.data.model.IntelligenceHalalStatus.UNKNOWN || halalAnalysis!!.status == com.example.halalyticscompose.data.model.IntelligenceHalalStatus.HALAL) {
                    // Coba AI jika lokal gagal atau lokal bilang halal (bisa jadi ada yang terlewat, walau lokal cepat)
                    // Atau kita biarkan lokal jika sudah jelas halal/haram. Mari kita minta AI jika syubhat atau bahan kurang jelas
                    kotlinx.coroutines.async { com.example.halalyticscompose.data.api.GroqApiClient.analyzeHalal(productName, ingredientsText) }
                } else null

                val ingredientsDeferred = if (ingredientsText.isNotBlank()) {
                    kotlinx.coroutines.async { com.example.halalyticscompose.data.api.GroqApiClient.analyzeIngredients(ingredientsText) }
                } else null

                val aiHalal = aiHalalDeferred?.await()
                if (aiHalal != null && (halalAnalysis == null || halalAnalysis!!.status == com.example.halalyticscompose.data.model.IntelligenceHalalStatus.UNKNOWN)) {
                    halalAnalysis = aiHalal
                }
                
                ingredientsAnalysis = ingredientsDeferred?.await()
            }

            // Fallback Halal Analysis if both failed
            if (halalAnalysis == null) {
                halalAnalysis = com.example.halalyticscompose.data.model.HalalAnalysisResult(
                    status = com.example.halalyticscompose.data.model.IntelligenceHalalStatus.UNKNOWN,
                    reason = "Tidak dapat memverifikasi status halal saat ini.",
                    suspiciousIngredients = emptyList()
                )
            }

            // 3. Health Score
            val offNutriments = com.example.halalyticscompose.data.model.OFFNutriments(
                energyKcal = product.getNutrimentNumber("energy-kcal_100g", "energy-kcal")?.toDoubleOrNull(),
                fat = product.getNutrimentNumber("fat_100g", "fat")?.toDoubleOrNull(),
                saturatedFat = product.getNutrimentNumber("saturated-fat_100g", "saturated_fat")?.toDoubleOrNull(),
                carbohydrates = product.getNutrimentNumber("carbohydrates_100g", "carbohydrates")?.toDoubleOrNull(),
                sugars = product.getNutrimentNumber("sugars_100g", "sugars")?.toDoubleOrNull(),
                fiber = product.getNutrimentNumber("fiber_100g", "fiber")?.toDoubleOrNull(),
                proteins = product.getNutrimentNumber("proteins_100g", "proteins")?.toDoubleOrNull(),
                salt = product.getNutrimentNumber("salt_100g", "salt")?.toDoubleOrNull(),
                sodium = product.getNutrimentNumber("sodium_100g")?.toDoubleOrNull()
            )

            val healthScore = com.example.halalyticscompose.data.local.HealthScoreCalculator.calculate(
                novaGroup = product.novaGroup as? Int ?: product.novaGroup?.toString()?.toIntOrNull(),
                nutriscoreGrade = product.nutriscoreGrade,
                nutriments = offNutriments
            )

            // 4. Alternatives if score is bad or non-halal
            if (healthScore.score < 70 || halalAnalysis!!.status != com.example.halalyticscompose.data.model.IntelligenceHalalStatus.HALAL) {
                val issues = buildString {
                    if (halalAnalysis!!.status != com.example.halalyticscompose.data.model.IntelligenceHalalStatus.HALAL) append("Bahan tidak halal/syubhat. ")
                    healthScore.factors.filter { it.impact.startsWith("-") }.forEach {
                        append("${it.name} (${it.value}). ")
                    }
                }
                alternatives = com.example.halalyticscompose.data.api.GroqApiClient.getAlternatives(productName, issues)
            }

            // 5. Build Result
            val result = com.example.halalyticscompose.data.model.ProductIntelligenceResult(
                barcode = product.barcode ?: product.id ?: "",
                name = productName,
                brands = product.brands,
                imageUrl = product.getBestImageUrl(),
                ingredientsText = ingredientsText,
                halalAnalysis = halalAnalysis!!,
                healthScore = healthScore,
                ingredientsAnalysis = ingredientsAnalysis,
                alternatives = alternatives,
                nutriments = offNutriments,
                novaGroup = product.novaGroup as? Int ?: product.novaGroup?.toString()?.toIntOrNull(),
                nutriscoreGrade = product.nutriscoreGrade
            )

            _intelligenceResult.value = result
            Log.d(TAG, "🤖 Intelligence Analysis Complete for: $productName")
        } catch (e: Exception) {
            Log.e(TAG, "Error in performIntelligenceAnalysis", e)
        }
    }
    
    // ========================================
    // UTILITY FUNCTIONS
    // ========================================
    
    /**
     * Clear search results
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchError.value = ""
        _totalCount.value = 0
        Log.d(TAG, "🧹 Search results cleared")
    }
    
    /**
     * Clear product detail
     */
    fun clearProductDetail() {
        _productDetail.value = null
        _intelligenceResult.value = null
        _detailError.value = ""
        Log.d(TAG, "🧹 Product detail cleared")
    }

    private suspend fun searchProductsDirectFallback(query: String, pageSize: Int, page: Int): Boolean {
        return try {
            val products = fetchProductsDirectFallback(query, pageSize, page)
            if (products.isNotEmpty()) {
                _searchResults.value = products
                _totalCount.value = products.size
                _searchError.value = ""
                Log.d(TAG, "✅ Fallback OpenFoodFacts result: ${products.size}")
                true
            } else {
                _searchResults.value = emptyList()
                _totalCount.value = 0
                _searchError.value = "Produk tidak ditemukan di OpenFoodFacts."
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback OpenFoodFacts failed: ${e.message}", e)
            false
        }
    }

    private suspend fun fetchProductsDirectFallback(query: String, pageSize: Int, page: Int): List<ProductItem> {
        return try {
            val response = openFoodFactsApiService.searchProducts(query = query, page = page, pageSize = pageSize)
            if (response.isSuccessful) {
                response.body()?.products ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch direct OpenFoodFacts failed: ${e.message}", e)
            emptyList()
        }
    }

    private suspend fun fetchBackendProductsPaged(
        query: String,
        pageSize: Int,
        startPage: Int = 1
    ): List<ProductItem> {
        val collected = mutableListOf<ProductItem>()
        for (currentPage in startPage until (startPage + MAX_PAGES_TO_FETCH)) {
            val response = externalApiService.searchProducts(query = query, pageSize = pageSize, page = currentPage)
            if (!response.isSuccessful) break

            val body = response.body()
            val content = body?.content
            if (body?.responseCode != 200 || content == null || content.products.isEmpty()) break

            collected += content.products
            if (content.products.size < pageSize) break
        }
        val merged = LinkedHashMap<String, ProductItem>()
        collected.forEach { product ->
            val key = when {
                !product.code.isNullOrBlank() -> "code:${product.code}"
                !product.barcode.isNullOrBlank() -> "barcode:${product.barcode}"
                !product.id.isNullOrBlank() -> "id:${product.id}"
                else -> "name:${product.getDisplayName().trim().lowercase()}"
            }
            merged.putIfAbsent(key, product)
        }
        return merged.values.toList()
    }

    private fun mergeUniqueProducts(primary: List<ProductItem>, secondary: List<ProductItem>): List<ProductItem> {
        if (primary.isEmpty()) return secondary
        if (secondary.isEmpty()) return primary

        val merged = LinkedHashMap<String, ProductItem>()
        (primary + secondary).forEach { product ->
            val key = when {
                !product.code.isNullOrBlank() -> "code:${product.code}"
                !product.barcode.isNullOrBlank() -> "barcode:${product.barcode}"
                !product.id.isNullOrBlank() -> "id:${product.id}"
                else -> "name:${product.getDisplayName().trim().lowercase()}"
            }
            merged.putIfAbsent(key, product)
        }
        return merged.values.toList()
    }

    private suspend fun loadProductDetailDirectFallback(barcode: String): Boolean {
        return try {
            val response = openFoodFactsApiService.getProductDetail(barcode)
            if (response.isSuccessful) {
                val product = response.body()?.product
                if (product != null) {
                    _productDetail.value = product
                    _detailError.value = ""
                    
                    // Load advanced images
                    val result = getProductImagesUseCase(product.getDisplayName(), barcode, "external")
                    _productImageState.value = result
                    
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback product detail failed: ${e.message}", e)
            false
        }
    }
    
    /**
     * Set filter and perform search
     */
    fun applyFilter(filter: String, query: String = "") {
        when (filter) {
            "All" -> searchProducts(query)
            "Halal" -> searchHalalProducts(query)
            "Vegetarian" -> searchVegetarianProducts(query)
            "Vegan" -> searchVeganProducts(query)
            else -> searchProducts(query)
        }
    }
}
