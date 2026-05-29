package com.example.halalyticscompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.repository.AIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManualInputUiState(
    val isLoading: Boolean = false,
    val result: AnalysisResult? = null,
    val error: String? = null
)

data class AnalysisResult(
    val halalStatus: String = "",
    val halalScore: Int = 0,
    val healthStatus: String = "",
    val healthScore: Int = 0,
    val personalizedMessage: String = "",
    val recommendations: List<String> = emptyList(),
    val dangerousIngredients: List<String> = emptyList()
)

@HiltViewModel
class ManualInputViewModel @Inject constructor(
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualInputUiState())
    val uiState: StateFlow<ManualInputUiState> = _uiState.asStateFlow()

    fun analyzeProduct(
        name: String,
        brand: String,
        ingredients: String,
        category: String
    ) {
        viewModelScope.launch {
            _uiState.value = ManualInputUiState(isLoading = true)
            try {
                val result = aiRepository.analyzeManualInput(
                    name = name,
                    brand = brand,
                    ingredients = ingredients,
                    category = category
                )
                _uiState.value = ManualInputUiState(result = result)
            } catch (e: Exception) {
                _uiState.value = ManualInputUiState(
                    error = "Gagal menganalisis produk: ${e.localizedMessage}"
                )
            }
        }
    }
}
