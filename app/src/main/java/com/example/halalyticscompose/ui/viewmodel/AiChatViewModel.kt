package com.example.halalyticscompose.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halalyticscompose.data.api.ApiService
import com.example.halalyticscompose.data.model.AiChatRequest
import com.example.halalyticscompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(val role: String, val text: String)

data class AiChatUiState(
    val loading: Boolean = false,
    val messages: List<ChatMessage> = listOf(
        ChatMessage("assistant", "Halo! Saya AI Halalytics. Tanyakan apa saja tentang halal, gizi, produk, atau kesehatan Anda.")
    ),
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(AiChatUiState())
    val state: StateFlow<AiChatUiState> = _state.asStateFlow()

    fun send(message: String) {
        if (message.isBlank()) return
        val history = _state.value.messages + ChatMessage("user", message)
        _state.value = _state.value.copy(messages = history, loading = true, error = null)

        viewModelScope.launch {
            try {
                val token = session.getBearerToken()
                if (token.isNullOrBlank()) {
                    _state.value = _state.value.copy(loading = false, error = "Silakan login terlebih dahulu.")
                    return@launch
                }
                val res = api.sendAiChat(token, AiChatRequest(message))
                val reply = if (res.isSuccessful) {
                    res.body()?.reply?.takeIf { it.isNotBlank() }
                        ?: "Maaf, AI tidak dapat merespons. Coba lagi."
                } else {
                    "Layanan AI sibuk. Coba sebentar lagi."
                }
                _state.value = _state.value.copy(
                    loading = false,
                    messages = history + ChatMessage("assistant", reply)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message,
                    messages = history + ChatMessage(
                        "assistant",
                        "Koneksi bermasalah. Periksa internet Anda dan coba lagi."
                    )
                )
            }
        }
    }
}
