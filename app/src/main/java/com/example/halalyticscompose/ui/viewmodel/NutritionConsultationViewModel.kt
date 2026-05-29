package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsultationMessage(
    val id: Int,
    val text: String,
    val senderRole: String, // user, nutritionist
    val timestamp: String
)

data class ConsultationSession(
    val id: Int,
    val subject: String,
    val userName: String,
    val lastMessage: String,
    val status: String // open, closed
)

@HiltViewModel
class NutritionConsultationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ConsultationMessage>>(emptyList())
    val messages: StateFlow<List<ConsultationMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMessages(sessionId: Int) {
        // Implementation with real API
    }

    fun sendMessage(sessionId: Int, text: String, role: String) {
        // Implementation with real API
    }
}
