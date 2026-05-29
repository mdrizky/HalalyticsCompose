package com.example.halalyticscompose.repository

import com.example.halalyticscompose.viewmodel.AnalysisResult
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.utils.SessionManager
import com.example.halalyticscompose.data.model.AiAnalysisRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun analyzeManualInput(
        name: String,
        brand: String,
        ingredients: String,
        category: String
    ): AnalysisResult {
        val token = sessionManager.getAuthToken()
        val authHeader = if (token.isNullOrBlank()) "" else "Bearer $token"
        
        // Combine all info into the ingredients text for the AI to analyze properly
        val fullContext = "Product: $name\nBrand: $brand\nCategory: $category\nIngredients: $ingredients"
        
        val request = mapOf("ingredients" to fullContext)

        val response = apiService.analyzeIngredients(authHeader, request)
        
        if (response.isSuccessful && response.body()?.success == true) {
            val content = response.body()!!.data!!
            return AnalysisResult(
                halalStatus = content.statusHalal,
                halalScore = content.healthScore.toDouble(), 
                healthStatus = content.healthStatus ?: "Unknown",
                healthScore = content.healthScore,
                personalizedMessage = content.personalizedMessage ?: "",
                recommendations = content.recommendations ?: emptyList(),
                dangerousIngredients = content.watchouts
            )
        } else {
            throw Exception("Failed to analyze product: ${response.message()}")
        }
    }
}
