package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.database.ProductHistoryEntity
import com.example.halalyticscompose.data.database.ProductRepository
import com.example.halalyticscompose.data.model.AddFavoriteRequest
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    val favoriteProducts = repository.getFavoriteProducts()
    
    init {
        syncFavoritesFromApi()
    }
    
    fun toggleFavorite(barcode: String) {
        viewModelScope.launch {
            try {
                val product = repository.getProductByBarcode(barcode)
                product?.let {
                    if (it.isFavorite) {
                        repository.removeFromFavorites(barcode)
                        // Sync with server
                        val token = sessionManager.getAuthToken()
                        val serverId = it.favoriteServerId
                        if (token != null && serverId != null) {
                            apiService.deleteFavorite("Bearer $token", serverId)
                        }
                    } else {
                        repository.addToFavorites(barcode)
                        // Sync with server
                        val token = sessionManager.getAuthToken()
                        if (token != null) {
                            val request = AddFavoriteRequest(
                                favoritable_id = 0, // In real case, we need product ID from server
                                favoritable_type = "App\\Models\\ProductModel",
                                product_name = it.name,
                                halal_status = it.status,
                                product_image = it.image
                            )
                            val response = apiService.addFavorite("Bearer $token", request)
                            if (response.isSuccessful && response.body()?.success == true) {
                                val serverId = response.body()?.data?.id
                                repository.updateFavoriteServerId(barcode, serverId)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Toggle favorite failed", e)
            }
        }
    }

    fun deleteProduct(barcode: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(barcode)
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Delete product failed", e)
            }
        }
    }
    private fun syncFavoritesFromApi() {
        viewModelScope.launch {
            val token = sessionManager.getAuthToken() ?: return@launch
            _isLoading.value = true
            try {
                val response = apiService.getFavorites("Bearer $token")
                if (response.success) {
                    response.data.forEach { favorite ->
                        favorite.barcode?.let { barcode ->
                            val entity = ProductHistoryEntity(
                                barcode = barcode,
                                name = favorite.productName,
                                status = favorite.halalStatus,
                                image = favorite.productImage,
                                timestamp = System.currentTimeMillis(),
                                isFavorite = true,
                                favoriteServerId = favorite.id,
                                isSynced = true
                            )
                            repository.insertProduct(entity)
                            repository.updateFavoriteServerId(barcode, favorite.id)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Sync favorites failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
