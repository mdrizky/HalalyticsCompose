package com.example.halalyticscompose.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OllamaApiService {
    @POST("api/chat")
    suspend fun chat(
        @Body request: OllamaChatRequest
    ): Response<OllamaChatResponse>
}

data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = false
)

data class OllamaChatResponse(
    val model: String,
    val message: OllamaMessage,
    val done: Boolean
)

data class OllamaMessage(
    val role: String,
    val content: String
)
