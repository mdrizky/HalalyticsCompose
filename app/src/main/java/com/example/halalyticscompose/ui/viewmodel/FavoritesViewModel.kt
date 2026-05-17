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

    private fun normalizeHalalForApi(status: String): String {
        return when (status.trim().lowercase()) {
            "halal" -> "halal"
            "haram", "tidak halal", "non-halal" -> "haram"
            else -> "syubhat"
        }
    }

    fun toggleFavorite(barcode: String) {
        viewModelScope.launch {
            try {
                val product = repository.getProductByBarcode(barcode) ?: return@launch
                val token = sessionManager.getAuthToken() ?: return@launch

                if (product.isFavorite) {
                    val serverId = product.favoriteServerId
                    repository.removeFromFavorites(barcode)
                    if (serverId != null) {
                        try {
                            val del = apiService.deleteFavorite("Bearer $token", serverId)
                            if (!del.isSuccessful) {
                                repository.addToFavorites(barcode)
                                repository.updateFavoriteServerId(barcode, serverId)
                                _errorMessage.value = "Gagal menghapus favorit di server."
                            }
                        } catch (e: Exception) {
                            repository.addToFavorites(barcode)
                            repository.updateFavoriteServerId(barcode, serverId)
                            _errorMessage.value = "Gagal sinkron: ${e.message}"
                        }
                    }
                } else {
                    val previous = product
                    repository.addToFavorites(barcode)
                    val request = AddFavoriteRequest(
                        favoritable_id = null,
                        barcode = barcode,
                        favoritable_type = "App\\Models\\ProductModel",
                        product_name = previous.name,
                        halal_status = normalizeHalalForApi(previous.status),
                        product_image = previous.image
                    )
                    try {
                        val response = apiService.addFavorite("Bearer $token", request)
                        if (response.isSuccessful && response.body()?.success == true) {
                            val serverId = response.body()?.data?.id
                            if (serverId != null) {
                                repository.updateFavoriteServerId(barcode, serverId)
                            }
                        } else {
                            repository.removeFromFavorites(barcode)
                            _errorMessage.value = response.body()?.message ?: "Gagal menambah favorit."
                        }
                    } catch (e: Exception) {
                        repository.removeFromFavorites(barcode)
                        _errorMessage.value = "Gagal sinkron: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e("FavoritesVM", "Toggle favorite failed", e)
                _errorMessage.value = e.message
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
