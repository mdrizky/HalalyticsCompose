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
        
        val request = AiAnalysisRequest(ingredientsText = fullContext)

        val response = apiService.analyzeIngredients(authHeader, request)
        
        if (response.success) {
            val content = response.content
            return AnalysisResult(
                halalStatus = content?.status ?: "Unknown",
                halalScore = content?.confidence ?: 0, 
                healthStatus = content?.healthRisk ?: "Unknown",
                healthScore = content?.confidence ?: 0,
                personalizedMessage = content?.analysis ?: "",
                recommendations = content?.redFlags ?: emptyList(),
                dangerousIngredients = content?.redFlags ?: emptyList()
            )
        } else {
            throw Exception("Failed to analyze product: ${response.message}")
        }
    }
}
