package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.model.*
import com.example.halalyticscompose.data.repository.ProductRepository
import com.example.halalyticscompose.domain.usecase.GetProductImagesUseCase
import com.example.halalyticscompose.data.model.ProductImageResult
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val getProductImagesUseCase: GetProductImagesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _productState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val productState: StateFlow<ProductUiState> = _productState.asStateFlow()

    private val _productImageState = MutableStateFlow<ProductImageResult?>(null)
    val productImageState: StateFlow<ProductImageResult?> = _productImageState.asStateFlow()

    private val _alternativesState = MutableStateFlow<AlternativesUiState>(AlternativesUiState.Initial)
    val alternativesState: StateFlow<AlternativesUiState> = _alternativesState.asStateFlow()

    fun loadProduct(barcode: String) {
        viewModelScope.launch {
            _productState.value = ProductUiState.Loading
            val token = sessionManager.getAuthToken()
            
            repository.getProductWithHalalInfo(barcode, token)
                .onSuccess { product ->
                    _productState.value = ProductUiState.Success(product)
                    
                    val source = if (product.halalInfo?.source == "open_food_facts") "external" else "local"
                    val result = getProductImagesUseCase(product.name, product.barcode, source)
                    _productImageState.value = result
                }
                .onFailure { error ->
                    _productState.value = ProductUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun recheckHalalStatus(product: Product) {
        viewModelScope.launch {
            repository.checkHalalStatus(
                product.barcode,
                product.name,
                product.brand
            ).onSuccess { halalInfo ->
                val updatedProduct = product.copy(halalInfo = halalInfo)
                _productState.value = ProductUiState.Success(updatedProduct)
            }
        }
    }
    
    fun refreshProduct(barcode: String) {
        loadProduct(barcode)
    }

    fun loadAlternatives(barcode: String) {
        viewModelScope.launch {
            _alternativesState.value = AlternativesUiState.Loading
            val token = sessionManager.getAuthToken()
            repository.getProductAlternatives(barcode, token)
                .onSuccess { alternatives ->
                    _alternativesState.value = AlternativesUiState.Success(alternatives)
                }
                .onFailure { error ->
                    _alternativesState.value = AlternativesUiState.Error(error.message ?: "Failed to get alternatives")
                }
        }
    }
}

sealed class ProductUiState {
    object Loading : ProductUiState()
    data class Success(val product: Product) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

sealed class AlternativesUiState {
    object Initial : AlternativesUiState()
    object Loading : AlternativesUiState()
    data class Success(val response: HalalAlternativeResponse) : AlternativesUiState()
    data class Error(val message: String) : AlternativesUiState()
}
